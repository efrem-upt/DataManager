window.addEventListener('load', function()
{
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

    updateLiveData = function()
    {
        // Date string is appended as a query with live data 
        // for not to use the cached version 
        var url = 'http://localhost:8080/dashboard/google/status'
        xhr = getXmlHttpRequestObject();
        xhr.onreadystatechange = evenHandler;
        // asynchronous requests
        xhr.open("GET", url, true);
        // Send the request over the network
        xhr.send(null);
    };

    function evenHandler()
    {
        // Check response is ready or not
        if(xhr.readyState == 4 && xhr.status == 200)
        {
            var json = JSON.parse(xhr.responseText);
            if (json.message != null) {
                document.getElementById('statusImage_loading').outerHTML = json.image;
                document.getElementById('infoGoogle_loading').outerHTML = json.message;
                clearInterval(timer);
                return;
            }
        }
        if (timer == null) {
             timer = setInterval(updateLiveData, 5000);
        }
    }
});