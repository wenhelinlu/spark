function setCookieGDPR(name,value)
{
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
	var url = document.location.href;
	var domain = '';
	console.log(url);
	if(url.indexOf("toutiaoabc") != -1){
		domain = "toutiaoabc.com";
	}
	else if(url.indexOf("6park.com") != -1){
		domain = "6park.com";
	}
	else if(url.indexOf("6parker.com") != -1){
		domain = "6parker.com";
	}
	console.log(domain);
	if(domain == ''){
		document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString()+";path=/";
	}
	else{
		document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString()+";domain="+domain+";path=/";
	}
}

function getCookieGDPR(name)
{
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
    if(arr=document.cookie.match(reg))
        return unescape(arr[2]);
    else
        return null;
}


$(function(){
    var hasCookie =  getCookieGDPR("showgdpr");
    var scode = getCookieGDPR("scode");
    var newsTmpName = getCookieGDPR("news_tmp_name");
    var url = "/pub/gdpr_area.php?act=get&news_tmp_name="+newsTmpName+"=&scode="+scode;
    $.ajax({
        type:"get",
        dataType : 'json',
        url: url,
        success: function (data) {
            var showPage = data["iseu"];
            if(showPage === 1 && hasCookie != 1){
                setCookieGDPR("showgdpr",0);
               addPrompt();
            }else{
                $("script[src='"+"public/js/prompt.js']").remove();
            }
        },
        error: function(xhr,type){
            console.log(xhr);
            console.log(type);
        }
    })
});
function addPrompt(){
    var html,
        html=' <div class="prompt-cookie" id="promptCookie"> <div class="prompt-cookie-a"> ';
    html+='<p>网站使用cookies技术来保障用户登录和记录访问数据, 继续使用本网站需要同意数据隐私政策</p>';
    html+='<div class="prompt-cookie-btn">';
    html+='<a href="/pub/gdpr.php">隐私条款详情</a>';
    html+='<div><button id="agreeCookie">同意隐私条款</button>';
//          html+='<a href="privacy-text.html" ><button>阅读条款详情</button></a>';
    html+='</div></div></div></div>';
    $("body").append(html);
    $("#agreeCookie").click(function(){
        $("#promptCookie").hide();
        setCookieGDPR("showgdpr",1);
        var scode = getCookieGDPR("scode");
        var newsTmpName = getCookieGDPR("news_tmp_name");
        var url = "/pub/gdpr_area.php?act=put&news_tmp_name="+newsTmpName+"=&scode="+scode;
		$("body").append("<img src='https://area.6parker.com/pub/syncgdpr.php' style='display:none' />");
		$("body").append("<img src='https://news.toutiaoabc.com/pub/syncgdpr.php' style='display:none' />");
		$("body").append("<img src='https://mv.6park.com/pub/syncgdpr.php' style='display:none' />");
		$("body").append("<img src='https://www.cool18.com/pub/syncgdpr.php' style='display:none' />");
        $.ajax({
            type:"put",
            dataType : 'json',
            url:url,
            success: function (data) {
                console.log(data)
            },
            error: function(xhr,type){
                console.log(xhr);
                console.log(type);
            }
        })
    })
}
