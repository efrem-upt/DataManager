window.addEventListener('load', function()
{
    var xhr = null;

    getXmlHttpRequestObject = function()
    {
        if(!xhr)
        {
            // Create a new XMLHttpRequest object
            xhr = new XMLHttpRequest();
        }
        return xhr;
    };

    getDomainStatus = function(row, callback)
    {
        // Date string is appended as a query with live data
        // for not to use the cached version
        var url = 'http://localhost:8080/user/possible-action/' + row['name'];
        xhr = getXmlHttpRequestObject();
        xhr.onreadystatechange = function() {
            if(xhr.readyState == 4 && xhr.status == 200)
            {
                var json = JSON.parse(xhr.responseText);

            }

        };
        // asynchronous requests
        xhr.open("GET", url, true);
        // Send the request over the network
        xhr.send(null);
    };
});