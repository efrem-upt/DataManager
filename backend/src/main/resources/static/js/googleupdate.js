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
        var url = 'http://localhost:8080/dashboard/google'
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
            parser = new DOMParser();
            var doc = parser.parseFromString(xhr.responseText, "text/html");
            var success_image = doc.getElementById('statusImage_success');
            var success_info = doc.getElementById('infoGoogle_success');
            if (success_info != null) {
                document.getElementById('statusImage_loading').outerHTML = success_image.outerHTML;
                document.getElementById('infoGoogle_loading').outerHTML = success_info.outerHTML;
                clearInterval(timer);
                return;
            }
        }
        if (timer == null) {
             timer = setInterval(updateLiveData, 5000);
        }
    }
});