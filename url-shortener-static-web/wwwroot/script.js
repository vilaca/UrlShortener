//
// js script for htt://go.pt
//
// vilaca - 20/Jul/2013 - initial version
// vilaca - 25/Oct/2014 - rewrite, multiple requests for one hash
// vilaca - 25/Oct/2014 - rewrite, multiple requests for one hash
//
function getUrlFromForm() {
    var url = f.v.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '')
    if ((url.indexOf('http://') !== 0) && (url.indexOf('https://') !== 0)) {
        return 'http://' + url
    }
    return url
}

function isValidUrl(url) {
    return url.indexOf('http://go2.pt') !== 0 && url.indexOf('http://www.go2.pt') !== 0
}

function inputError() {
    f.v.style.borderColor = 'red'
    error.innerHTML = 'Invalid URL!'
}

function inputForbidden(reason) {
    f.v.style.borderColor = 'red'
    if (reason === 'malware')
        error.innerHTML = 'Forbidden URL - Suspected Malware'
    else if (reason === 'phishing')
        error.innerHTML = 'Forbidden URL - Suspected Phishing'
}

function inputDisable() {
    f.v.enabled = false;
    f.s.enabled = false;
}

function inputEnable() {
    f.v.enabled = true;
    f.s.enabled = true;
}

function clearError() {
    f.v.style.borderColor = '#444'
    error.innerHTML = '&nbsp;'
}

function submit(su) {
    var http = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP")
    http.onreadystatechange = function() {
        if (http.readyState == 4) {
            inputEnable()
            if (http.status == 200) {
                if (http.responseText !== '') {
                    error.innerHTML = '&nbsp;'
                        // place result in form and select text
                    f.v.value = 'go2.pt/' + http.responseText
                    f.v.focus()
                    f.v.select()
                } else {
                    error.innerHTML = 'Please try again.'
                }
            } else if (http.status == 202) {
                submit(su)
            } else if (http.status == 400 || http.status == 500) {
                inputError()
            } else if (http.status == 403) {
                inputForbidden(http.responseText)
            } else {
                error.innerHTML = 'Please try again.'
            }
        }
    };
    http.open('PUT', '/rest/v1/', true)
    http.send('v=' + su)
    error.innerHTML = 'Processing...'
}

window.onload = function() {
    f.v.onfocus = function() {
        clearError()
    };
    f.v.onclick = function() {
        f.v.select()
    };
    f.v.onchange = function() {
        clearError()
    };
    f.onsubmit = function() {
        // do basic validation, server will still refuse invalid Urls
        var su = getUrlFromForm()
        if (!isValidUrl(su)) {
            inputError()
        } else {
            f.v.style.color = 'black'
            inputDisable();
            submit(su);
        }
        // returning 'false' cancels HTML basic submit behavior
        return false
    };
};