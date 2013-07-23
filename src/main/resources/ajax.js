
// client side js for go.pt
// @author vilaca - 20/July/2013

function getUrlFromForm() {

    var url = f.v.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');

    if (url.indexOf('http') !== 0) {
        return 'http://' + url;
    }

    return url;
}

//Based on
//http://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-an-url
function isValidUrl(url) {

    if (url.indexOf('http://go2.pt') === 0) {
        return false;
    }

    var pattern = new RegExp('^(https?:\/\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?(\/[-a-z\\d%_.~+]*)*' + // port and path
    '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
    '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locater

    return pattern.test(url);
}

window.onload = function () {

    f.v.onfocus = function () {
        f.v.style.color = 'black';
    };

    f.onsubmit = function () {

        // do basic validation, server will still refuse invalid Urls

        var su = getUrlFromForm();

        if (!isValidUrl(su)) {
        
            f.v.style.color = 'red';
        
        } else {
            f.v.style.color = 'black';

            var params = 'v=' + su;

            var http = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");

            http.onreadystatechange = function () {

                if (http.readyState == 4 && http.status == 200) {

                    if (http.responseText === undefined || http.responseText === '') return false;

                    // place result in form and select text
                    f.v.value = '[$shortlinkdomain$]' + http.responseText;
                    f.v.focus();
                    f.v.select();

                }
			};

            http.open('POST', '/new', true);
            http.send(params);

        };
  
        // returning 'false' cancels HTML basic submit behavior
        return false;
    };
};