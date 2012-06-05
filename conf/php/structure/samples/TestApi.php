<?php

include_once( dirname(__FILE__).'/../APIClient.php');
include_once( dirname(__FILE__).'/../AntAPI.php');
include_once( dirname(__FILE__).'/../StorageAPI.php');

    $privateKey = "<PRIVATE_KEY>";
	$userId = "<CLIENT_ID>";
	$fileId = "<FILE_ID>";
	$apiClient = new APIClient($privateKey, "https://dev-api.groupdocs.com/v2.0");

	$api = new AntAPI($apiClient);

    // $postData = new AnnotationInfo();
    // $postData->Type = "0";
    // $box = new Rectangle();
    // $box->Height = 200;
    // $box->Width = 200;
    // $box->X = 200;
    // $box->Y = 200;
    // $postData->Box = $box;
    // $reply = new AnnotationReplyInfo();
    // $reply->Message = "sent from php client";
    // $postData->Replies = array($reply);
    // $response = $api->CreateAnnotation($userId, $fileId, $postData);
	// print_r($response);

	// $response = $api->ListAnnotations($userId, $fileId);

	// test file upload
	$api = new StorageAPI($apiClient);
	$api->Upload($userId, "test.docx", "uploaded", "file://".dirname(__FILE__)."/test.docx");

?>

