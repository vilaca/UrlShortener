
// client side js for go.pt
// @author vilaca - 20/July/2013

function getUrlFromForm() {

    var url = f.v.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '')
	
    if ((url.indexOf('http://') !== 0) && (url.indexOf('https://') !== 0)) {
        return 'http://' + url
    }

    return url
}

function isValidUrl(url) {

	return url.indexOf('http://go2.pt') !== 0

}

window.onload = function () {

    f.v.onfocus = function () {
        f.v.style.color = 'black'
    };

    f.onsubmit = function () {

        // do basic validation, server will still refuse invalid Urls

        var su = getUrlFromForm()

        if (!isValidUrl(su)) {
        
            f.v.style.color = 'red'
        
        } else {
            f.v.style.color = 'black'

            var params = 'v=' + su

            var http = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP")

            http.onreadystatechange = function () {

                if (http.readyState == 4 )
                {
                 	if ( http.status == 200)
                 	{
						if (http.responseText !== 'BAD-URI')
						{
        	            	// place result in form and select text
            	        	f.v.value = '[$shortlinkdomain$]' + http.responseText
                	    	f.v.focus()
                    		f.v.select()
                    	}
                    	else
                    	{
				            f.v.style.color = 'red'
                    	}
					}
                }
			};

            http.open('POST', '/new', true)
            http.send(params)

        };
  
        // returning 'false' cancels HTML basic submit behavior
        return false
    };
};