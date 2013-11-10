
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

function inputError()
{
	f.v.style.borderColor = 'red'
	error.innerHTML = 'Invalid URL!'
}

function inputForbidden()
{
	f.v.style.borderColor = 'red'
	error.innerHTML = 'Forbidden URL - Phishing'
}

function inputDisable()
{
	f.v.enabled = false;
	f.s.enabled = false;
}

function inputEnable()
{
	f.v.enabled = true;
	f.s.enabled = true;
}

function clearError()
{
	f.v.style.borderColor = '#444'
	error.innerHTML = '&nbsp;'
}

window.onload = function () {

    f.v.onfocus = function () {
        clearError()
    };

	f.v.onclick = function() {
		f.v.select()
	};

	f.v.onchange = function() {
		clearError()
	};

    f.onsubmit = function () {

        // do basic validation, server will still refuse invalid Urls

        var su = getUrlFromForm()

        if (!isValidUrl(su)) {
        
            inputError()
        
        } else {
            f.v.style.color = 'black'

            var params = 'v=' + su

            var http = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP")


			var tmr = setTimeout( function() {
				
				http.abort()
				error.innerHTML = 'Please try again.'
    			inputEnable()
    		 
			},2000)

            http.onreadystatechange = function () {

                if (http.readyState == 4 )
                {
					inputEnable()
                	window.clearTimeout(tmr)
                
                 	if ( http.status == 200)
                 	{
						if (http.responseText !== '')
						{
        	            	// place result in form and select text
            	        	f.v.value = '[$shortlinkdomain$]' + http.responseText
                	    	f.v.focus()
                    		f.v.select()
                    	}
                    }
                    else if ( http.status == 400)
                   	{
				        inputError()
                   	}
                    else if ( http.status == 403)
                   	{
				        inputForbidden()
                   	}
                   	else
                   	{
                   		error.innerHTML = 'Please try again.'
                   	}
                }
			};

			inputDisable()

            http.open('POST', '/new', true)
            
            http.send(params)

        };
  
        // returning 'false' cancels HTML basic submit behavior
        return false
    };
};