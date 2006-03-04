var CONFIGURE_ME = '<span id="catcheye" onclick="showPreferences()">Please configure me!</span>';

var flipShown = false,
	configured = false,
	animation = {duration:0, starttime:0, to:1.0, now:0.0, from:0.0, firstElement:null, timer:null},
	bouncer = {duration:0, starttime:0, to:1.0, now:0.0, from:0.0, firstElement:null, timer:null, times:1};	

function setup()
{
	createGenericButton(document.getElementById("done"), "Done", hidePreferences);
	loadPreferences();
	
	if (window.widget)
	{
		widget.onremove = clearPreferences;
	}
}

function bookmarkSelectionChanged(popup) 
{
	if (window.Plugin) {
		document.getElementById("nickname").value = Plugin.nicknameAtIndex(popup.selectedIndex);
		document.getElementById("server").value = Plugin.hostnameAtIndex(popup.selectedIndex);
		document.getElementById("user").value = Plugin.usernameAtIndex(popup.selectedIndex);
		document.getElementById("path").value = Plugin.pathAtIndex(popup.selectedIndex);
		document.getElementById("protocol").value = Plugin.protocolAtIndex(popup.selectedIndex);
	}
}

function savePreferences()
{
	var nickname =   document.getElementById("nickname").value || null;
	var server =   document.getElementById("server").value || null;
	var user =     document.getElementById("user").value || null;
	var path =     document.getElementById("path").value || null;
	var protocol = document.getElementById("protocol").value || null;
	
	document.getElementById("serverlabel").innerHTML = nickname || CONFIGURE_ME;
	
	configured = !!server;
	
	if (verifyAccount())
	{	
		widget.setPreferenceForKey(nickname, widget.identifier + "@nickname");
		widget.setPreferenceForKey(server, widget.identifier + "@server");
		widget.setPreferenceForKey(user, widget.identifier + "@user");
		widget.setPreferenceForKey(path, widget.identifier + "@path");
		widget.setPreferenceForKey(protocol, widget.identifier + "@protocol");
	}
}

function loadPreferences()
{
	var nickname = widget.preferenceForKey(widget.identifier + "@nickname");
	var server = widget.preferenceForKey(widget.identifier + "@server");
	var user = widget.preferenceForKey(widget.identifier + "@user");
	var path = widget.preferenceForKey(widget.identifier + "@path");
	var protocol = widget.preferenceForKey(widget.identifier + "@protocol");

	document.getElementById("nickname").value = nickname || "";
	document.getElementById("server").value = server || "";
	document.getElementById("user").value = user || "";
	document.getElementById("path").value = path || "";
	document.getElementById("protocol").value = protocol || "";

	document.getElementById("serverlabel").innerHTML = nickname || CONFIGURE_ME;
	configured = !!server;
}

function clearPreferences()
{
	widget.setPreferenceForKey(null, widget.identifier + "@server");
	widget.setPreferenceForKey(null, widget.identifier + "@user");
	widget.setPreferenceForKey(null, widget.identifier + "@path");
	widget.setPreferenceForKey(null, widget.identifier + "@protocol");
}

function showPreferences() 
{
	var front = document.getElementById("front");
	var back = document.getElementById("back");

	widget.prepareForTransition("ToBack");
       
	front.style.display="none";
	back.style.display="block";

	if (window.widget)
		setTimeout ("widget.performTransition();", 0);
}

function hidePreferences() 
{
	savePreferences();
	hideObj("fliprollie");

	var front = document.getElementById("front");
	var back = document.getElementById("back");

	widget.prepareForTransition("ToFront");

	back.style.display="none";
	front.style.display="block";

	if (window.widget) {
		setTimeout ("widget.performTransition();", 0);
	}
}

function mousemove (event) 
{
	if (!flipShown) {
		if (animation.timer != null) {
			clearInterval (animation.timer);
			animation.timer  = null;
		}
 
		var starttime = (new Date).getTime() - 13;
 
		animation.duration = 500;
		animation.starttime = starttime;
		animation.firstElement = document.getElementById ("flip");
		animation.timer = setInterval ("animate();", 13);
		animation.from = animation.now;
		animation.to = 1.0;
		animate();
		flipShown = true;
	}
}

function mouseexit (event) 
{
	if (flipShown) {
		// fade in the info button
		if (animation.timer != null) {
			clearInterval (animation.timer);
			animation.timer  = null;
		}

		var starttime = (new Date).getTime() - 13;

		animation.duration = 500;
		animation.starttime = starttime;
		animation.firstElement = document.getElementById ("flip");
		animation.timer = setInterval ("animate();", 13);
		animation.from = animation.now;
		animation.to = 0.0;
		animate();
		flipShown = false;
	}
}

function animate() 
{
	var T;
	var ease;
	var time = (new Date).getTime();
   

	T = limit_3(time-animation.starttime, 0, animation.duration);

	if (T >= animation.duration) {
		clearInterval (animation.timer);
		animation.timer = null;
		animation.now = animation.to;
	}
	else {
		ease = 0.5 - (0.5 * Math.cos(Math.PI * T / animation.duration));
		animation.now = computeNextFloat (animation.from, animation.to, ease);
	}

	animation.firstElement.style.opacity = animation.now;
}

function bounce() 
{
	var T;
	var ease;
	var time = (new Date).getTime();

	T = limit_3(time-bouncer.starttime, 0, bouncer.duration * bouncer.times);

	if (T >= bouncer.duration * bouncer.times) {
		clearInterval (bouncer.timer);
		bouncer.timer = null;
		bouncer.now = bouncer.from;
	}
	else {
		ease = Math.abs(Math.sin(Math.PI * T / bouncer.duration));
		bouncer.now = computeNextInt (bouncer.from, bouncer.to, ease);
	}

	bouncer.firstElement.style.backgroundPositionY = bouncer.now + "px";
}

function bounceDuckStop()
{
	if (bouncer.timer != null) {
		clearInterval (bouncer.timer);
		bouncer.timer  = null;
		bouncer.firstElement.style.backgroundPositionY = "30px";
	}
}

function bounceDuckStart(times) 
{
	bounceDuckStop();

	var starttime = (new Date).getTime() - 11;

	bouncer.duration = times != 1 ? 650 : 500;
	bouncer.starttime = starttime;
	bouncer.firstElement = document.getElementById ("duck");
	bouncer.timer = setInterval ("bounce();", 11);
	bouncer.from = 30;
	bouncer.to = 0;
	bouncer.times = times || 1;
	bounce();
}

function showObj(id) { // shows an element
	document.getElementById(id).style.display = "block";
}

function hideObj(id) { // hides an element
	document.getElementById(id).style.display = "none";
}

function limit_3 (a, b, c) {
	return a < b ? b : (a > c ? c : a);
}

function computeNextFloat (from, to, ease) {
	return from + (to - from) * ease;
}

function computeNextInt (from, to, ease) {
	return Math.round(from + (to - from) * ease);
}

function enterflip(event) {
	showObj("fliprollie");
}

function exitflip(event) {
	hideObj("fliprollie");
}

function verifyAccount()
{
	// Not yet implemented
	
	return true;

	var nickname =   document.getElementById("nickname").value || null;
	var server =   document.getElementById("server").value || null;
	var user =     document.getElementById("user").value || null;
	var path =     document.getElementById("path").value || null;
	var protocol = document.getElementById("protocol").value || null;

	var command = "/usr/bin/" + protocol + " " + user + "@" + server;
	alert(command);

	var oString = widget.system(command, null).outputString;
	alert(oString);
}

/**
 *	Drag and drop handlers
 */

function dragdrop (event) {
	var uri = null;

	try {
		uri = event.dataTransfer.getData("text/uri-list");	// attempt to load the URL
	} 
	catch (e) {
		alert(e);
	}

	if (uri && configured)
	{
		var droppedfiles= uri.split("\n");
		for(i = 0; i < droppedfiles.length; i++) {
			localfile = droppedfiles[i];
			localfile = localfile.substr(localfile.indexOf("localhost") + 9);
			transfer(localfile);
		}
	}
	else
	{
		bounceDuckStart(1);
	}
	
	event.stopPropagation();
	event.preventDefault();
}

// The dragenter, dragover, and dragleave functions are implemented but not used.  They
// can be used if you want to change the image when it enters the widget.

function dragenter (event) {
	event.stopPropagation();
	event.preventDefault();
}

function dragover (event) {
	event.stopPropagation();
	event.preventDefault();
}

function dragleave (event) {
	event.stopPropagation();
	event.preventDefault();
}

function transfer(file)
{
	var server =   document.getElementById("server").value || null;
	var user =     document.getElementById("user").value || null;
	var path =     document.getElementById("path").value || null;
	var protocol = document.getElementById("protocol").value || null;

	bounceDuckStart(1000);	

	var command = "/usr/bin/osascript Scripts/Upload.scpt"
		+ " " + protocol
		+ " " + server
		+ " " + user
		+ " " + path
		+ " " + file;
	widget.system(command, bounceDuckStop);
}
