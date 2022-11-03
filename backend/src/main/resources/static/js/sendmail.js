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

sendMailToService = function(domain, contact_email)
{
    // Date string is appended as a query with live data
    // for not to use the cached version
    console.log(contact_email);
    var url = 'http://localhost:8080/user/send-email';
    xhr = getXmlHttpRequestObject();
    // asynchronous requests
    xhr.open("POST", url, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    // Send the request over the network
    xhr.send('domain=' + domain + '&email=' + contact_email);
};