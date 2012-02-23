class $resource$

$methods:{ method |
  def self.$method.name$($if(method.postObject)$body,$endif$ $if(method.pathParameters)$$method.pathParameters:{ param | $param.name$};separator=", "$, $endif$opts={})
$if(method.queryParameters)$    query_param_keys=[$method.queryParameters:{ param | :$param.name$};separator=", "$]$endif$

    # verify existence of params
    $if(method.pathParameters)$$method.pathParameters:{ param | raise "#{$param.name$} is required" if $param.name$.nil?};separator=", "$$endif$

    # set default values and merge with input
    # todo: do we know the types?
    options = {
$if(method.arguments)$
 $method.arguments:{arg | $if(arg.defaultValue)$     $arg.name$: "$arg.defaultValue$",$\r$$endif$}$
$endif$
    }.merge(opts)
  
    #resource path
    path = "$method.resourcePath$".sub('.{format}', '.json')
  
$if(method.pathParameters)$
    # sub pathparams
$method.pathParameters:{ param |
    path.sub!('{$param.name$}', URI.encode($param.name$.to_s))
}$$endif$
    #pull querystring keys from options
    queryopts = options.select do |key,value|
      query_param_keys.include? key
    end

$if(method.hasResponseValue)$
    # hasResponseValue
    response = Swagger::Request.new(:get, path, {:params=>queryopts,:headers=>nil, :body=>nil}).make
$if(method.returnValueList)$
    response.map {|result| $method.returnClassName$Model.new(result)}
$else$
    $method.returnClassName$Model.new(response.body)
$endif$
$else$
  $if(method.postObject)$
    if not body.nil?
      body=body.to_body
    end

    Swagger::Request.new(:post, path, {:params=>queryopts,:headers=>nil, :body=>body})
  $else$
    Swagger::Request.new(:get, path, {:params=>queryopts,:headers=>nil, :body=>nil})
  $endif$
$endif$

  end

}$
end
