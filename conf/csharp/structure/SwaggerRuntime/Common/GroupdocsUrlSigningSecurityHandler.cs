/**
 *  Copyright 2011 Wordnik, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;

namespace SwaggerRuntime.Common
{
  public class DynabicUrlSigningSecurityHandler : ISecurityHandler
  {
    private readonly string _clientKey = "";
    private readonly string _privateKey = "";
    public const string SIGNATURE_PARAM = "signature";
    public const string CLIENT_KEY_PARAM = "clientkey";

    public DynabicUrlSigningSecurityHandler(string clientKey, string privateKey)
    {
        if (string.IsNullOrEmpty(clientKey))
        {
            throw new ArgumentException("Argument can't be null nor empty.", "clientKey");
        }
        if (string.IsNullOrEmpty(privateKey))
        {
            throw new ArgumentException("Argument can't be null nor empty.", "privateKey");
        }

        _clientKey = clientKey;
        _privateKey = privateKey;
    }

    public static string SignString(string content, string signingKey)
    {
        var encoding = new UTF8Encoding();
        var signingKeyBytes = encoding.GetBytes(signingKey);
        var contentBytes = encoding.GetBytes(content.ToLower());

        // compute the hash
        HMACSHA1 algorithm = new HMACSHA1(signingKeyBytes);
        byte[] hash = algorithm.ComputeHash(contentBytes);

        // convert the bytes to string and Remove any trailing '=' characters
        return Convert.ToBase64String(hash).TrimEnd('=');
    }

    public static string SignUrl(string url, string clientKey, string signingKey)
    {
        char separator;
        if (url.IndexOf('?') == -1)
        {
            separator = '?';
        }
        else
        {
            separator = '&';
        }
        return url + string.Format("{0}{1}={2}", separator, SIGNATURE_PARAM, SignString(Uri.EscapeDataString(new Uri(url).PathAndQuery), signingKey));
    }

    #region ISecurityHandler members

    public string PopulateSecurityInfo(string toSign, IDictionary<string, string> httpHeaders)
    {
        return SignUrl(toSign, _clientKey, _privateKey);
    }

    #endregion
  }
}
