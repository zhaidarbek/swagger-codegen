
import sys
import os
import hmac

from AntAPI import AntAPI
from APIClient import APIClient

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/../')
import model


def listAnnotations():
    response = AntAPI(apiClient).ListAnnotations(userId, fileId)
    print(response.Status)

def createAnnotation():
    box = Rectangle()
    box.X = 100
    box.Y = 100
    box.Width = 100
    box.Height = 100
    reply = AnnotationReplyInfo()
    reply.Message = "Test message from python client"
    postData = AnnotationInfo()
    postData.Type = "0"
    postData.Box = box
    postData.Replies = [reply]

    response = AntAPI(apiClient).CreateAnnotation(userId, fileId, postData)
    print(response.Status)

def uploadFile():
    postData = "file://" + os.path.dirname(os.path.abspath(__file__)) + "/test.docx"
    response = StorageAPI(apiClient).Upload(userId, "python/test.docx", "uploaded from python client library", postData)
    print(response)


if __name__ == '__main__':
    privateKey = "<PRIVATE_KEY>"
    userId = "<CLIENT_ID>"
    fileId = "<FILE_ID>"
    apiClient = APIClient(privateKey, "https://dev-api.groupdocs.com/v2.0")

#    createAnnotation()
#    listAnnotations()
    uploadFile()

