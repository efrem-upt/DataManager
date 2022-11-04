var xhr = null;
var timer;

getXmlHttpRequestObject = function()
{
    if(!xhr)
    {
        // Create a new XMLHttpRequest object
        xhr = new XMLHttpRequest();
    }
    return xhr;
};

acceptSuggestionOfService = function(id)
{
    // Date string is appended as a query with live data
    // for not to use the cached version
    var url = 'http://localhost:8080/service/accept-suggestion';
    xhr = getXmlHttpRequestObject();
    // asynchronous requests
    xhr.open("POST", url, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    // Send the request over the network
    xhr.send('id=' + id);
};