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

    updateTable = function()
    {
        // Date string is appended as a query with live data
        // for not to use the cached version
        var url = 'http://localhost:8080/dashboard'
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
            var futureServiceTable = doc.getElementById('interactionsTable');
            if (futureServiceTable != null) {
                document.getElementById('interactionsTable').outerHTML = futureServiceTable.outerHTML;
                $('#interactionsTable').DataTable();
            }
        }
        if (timer == null) {
            timer = setInterval(updateTable, 1000);
        }
    }
});