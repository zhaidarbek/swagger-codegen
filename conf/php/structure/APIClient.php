<?php
/**
 * APIClient.php
 */


/* Autoload the model definition files */
/**
 *
 *
 * @param string $className the class to attempt to load
 */
function __autoload($className) {
	$currentDir = substr(__FILE__, 0, strrpos(__FILE__, '/'));
	if (file_exists($currentDir . '/' . $className . '.php')) {
		include $currentDir . '/' . $className . '.php';
	} elseif (file_exists($currentDir . '/../model/' . $className . '.php')) {
		include $currentDir . '/../model/' . $className . '.php';
	}
}


class APIClient {

	public static $POST = "POST";
	public static $GET = "GET";
	public static $PUT = "PUT";
	public static $DELETE = "DELETE";

	/**
	 * @param string $privateKey your Private key
	 * @param string $apiServer the address of the API server
	 */
	function __construct($privateKey, $apiServer) {
		$this->privateKey = $privateKey;
		$this->apiServer = $apiServer;
	}


    /**
	 * @param string $resourcePath path to method endpoint
	 * @param string $method method to call
	 * @param array $queryParams parameters to be place in query URL
	 * @param array $postData parameters to be placed in POST body
	 * @param array $headerParams parameters to be place in request header
	 * @return unknown
	 */
	public function callAPI($resourcePath, $method, $queryParams, $postData,
		$headerParams) {

		$headers = array();
		$headers[] = empty($postData) ? "Content-type: text/html" : "Content-type: application/json";

        # Allow API key from $headerParams to override default
        $added_api_key = False;
		if ($headerParams != null) {
			foreach ($headerParams as $key => $val) {
				$headers[] = "$key: $val";
			}
		}
		
		if (is_object($postData) or is_array($postData)) {
			$postData = json_encode(self::object_to_array($postData));
                        print $postData;
                        print "\n\r";
		}

		$url = $this->apiServer . $resourcePath;

		$curl = curl_init();
		curl_setopt($curl, CURLOPT_TIMEOUT, 5);
		// return the result on success, rather than just TRUE
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);

		if ($method == self::$GET) {
			if (! empty($queryParams)) {
				$url = ($url . '?' . http_build_query($queryParams));
			}
		} else if ($method == self::$POST) {
				curl_setopt($curl, CURLOPT_POST, true);
				curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			} else if ($method == self::$PUT) {
				$json_data = json_encode($postData);
				curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "PUT");
				curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			} else if ($method == self::$DELETE) {
				curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "DELETE");
				curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			} else {
			throw new Exception('Method ' . $method . ' is not recognized.');
		}

		curl_setopt($curl, CURLOPT_URL, self::sign($url));

		// Make the request
		$response = curl_exec($curl);
		// print $response;
		$response_info = curl_getinfo($curl);

		// Handle the response
		if ($response_info['http_code'] == 0) {
			throw new Exception("TIMEOUT: api call to " . $url .
				" took more than 5s to return" );
		} else if ($response_info['http_code'] == 200) {
			$data = json_decode($response);
		} else if ($response_info['http_code'] == 401) {
			throw new Exception("Unauthorized API request to " . $url .
					": ".json_decode($response)->message );
		} else if ($response_info['http_code'] == 404) {
			$data = null;
		} else {
			throw new Exception("Can't connect to the api: " . $url .
				" response code: " .
				$response_info['http_code']);
		}

		return $data;
	}



	/**
	 * Take value and turn it into a string suitable for inclusion in 
	 * the path or the header
	 * @param object $object an object to be serialized to a string
	 * @return string the serialized object
	 */
	public static function toPathValue($object) {
        if (is_array($object)) {
            return implode(',', $object);
        } else {
            return $object;
        }
	}


	/**
	 * Derialize a JSON string into an object
	 *
	 * @param object $object object or primitive to be deserialized
	 * @param string $class class name is passed as a string
	 * @return object an instance of $class
	 */
	public static function deserialize($object, $class) {

		if (in_array($class, array('string', 'int', 'float', 'bool'))) {
			settype($object, $class);
			return $object;
		} else {
			if(empty($class)){
				return;
			}
			$instance = new $class(); // this instantiates class named $class
			$classVars = get_class_vars($class);
		}

		foreach ($object as $property => $value) {

			// Need to handle possible pluralization differences
			$true_property = $property;

			if (! property_exists($class, $true_property)) {
				if (property_exists($class, ucfirst($property))) {
					$true_property = ucfirst($property);
				} else if (substr($property, -1) == 's') {
					$true_property = substr($property, 0, -1);
					if (! property_exists($class, $true_property)) {
						trigger_error("class $class has no property $property"
							. " or $true_property", E_USER_WARNING);
					}
				} else {
					trigger_error("class $class has no property $property",
						E_USER_WARNING);
				}
			}

			$type = $classVars['swaggerTypes'][$true_property];
			if (in_array($type, array('string', 'int', 'float', 'bool'))) {
				settype($value, $type);
				$instance->{$true_property} = $value;
			} elseif (preg_match("/array<(.*)>/", $type, $matches)) {
				$sub_class = $matches[1];
				$instance->{$true_property} = array();
				foreach ($value as $sub_property => $sub_value) {
					$instance->{$true_property}[] = self::deserialize($sub_value,
						$sub_class);
				}
			} else {
				$instance->{$true_property} = self::deserialize($value, $type);
			}
		}
		return $instance;
	}

        public static function object_to_array($data) {
            if (is_array($data) || is_object($data))
            {
                $result = array();
                foreach ($data as $key => $value)
                {
                    if(!is_null($value)){
                        $result[$key] = self::object_to_array($value);
                    }
                }
                return $result;
            }
            return $data;
        }

	public function sign($url) {
		$urlParts = parse_url($url);
		$pathAndQuery = $urlParts['path'].$urlParts['query'];
		$signature = base64_encode(hash_hmac("sha1", $pathAndQuery, $this->privateKey, true)); 
		if(substr($signature, -1) == '='){
			$signature = substr($signature, 0, - 1);
		}
		$url = $url . (empty($urlParts['query']) ? '?' : '&') . 'signature=' . $signature;
		return $url;
	}
	
}


?>
