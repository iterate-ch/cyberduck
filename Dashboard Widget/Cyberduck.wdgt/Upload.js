/**
 *	Cyberduck Dashboard widget
 *	Copyright (c) 2006 Claudio Procida & David V. Kocher. All rights reserved.
 *	http://cyberduck.ch/
 *
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	Bug fixes, suggestions and comments should be sent to:
 *	dkocher@cyberduck.ch
 */

var CONFIGURE_ME;

var flipShown = false,
	configured = false,
	animation = {duration:0, starttime:0, to:1.0, now:0.0, from:0.0, firstElement:null, timer:null},
	bouncer = {duration:0, starttime:0, to:1.0, now:0.0, from:0.0, firstElement:null, timer:null, times:1};	

function setup()
{
	CONFIGURE_ME =  '<span id="catcheye" onclick="showPreferences()">' + getLocalizedString("Please configure me!") + '</span>';
	createGenericButton(document.getElementById("done"), getLocalizedString("Done"), hidePreferences);
	if (window.widget)
	{
		widget.onremove = clearPreferences;
	}
	document.getElementById("server").setAttribute("placeholder", getLocalizedString("Server"));
	document.getElementById("user").setAttribute("placeholder", getLocalizedString("Username"));
	document.getElementById("path").setAttribute("placeholder", getLocalizedString("Path"));
	loadPreferences();
}

function getLocalizedString (key) {
	try {
		var ret = localizedStrings[key];
		if (ret === undefined) {
			ret = key;
		}
		return ret;
	}
	catch (ex) {
		//
	}
	return key;
}

function bookmarkSelectionChanged(popup) 
{
	if (window.Plugin) {
		document.getElementById("nickname").value = Plugin.nicknameAtIndex(popup.options[popup.selectedIndex].value);
		document.getElementById("server").value = Plugin.hostnameAtIndex(popup.options[popup.selectedIndex].value);
		document.getElementById("user").value = Plugin.usernameAtIndex(popup.options[popup.selectedIndex].value);
		document.getElementById("path").value = Plugin.pathAtIndex(popup.options[popup.selectedIndex].value);
		document.getElementById("protocol").value = Plugin.protocolAtIndex(popup.options[popup.selectedIndex].value);
	}
}

function savePreferences()
{
	var nickname =   document.getElementById("nickname").value || null;
	var server =   document.getElementById("server").value || null;
	var user =     document.getElementById("user").value || null;
	var path =     document.getElementById("path").value || null;
	var protocol = document.getElementById("protocol").value || null;
	
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

	document.getElementById("nickname").value = nickname || null;
	document.getElementById("server").value = server || null;
	document.getElementById("user").value = user || null;
	document.getElementById("path").value = path || null;
	document.getElementById("protocol").value = protocol || null;

	verifyAccount();
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
		// The duration of animation has reached the total time
		// needed by the required bounces. We reposition the duck
		// to its rest position.

		clearInterval (bouncer.timer);
		bouncer.timer = null;
		bouncer.now = bouncer.from;
	}
	else if (bouncer.shouldStop) {
		// We adjust the number of bounces, bouncer.times, to finish
		// the current bounce

		bouncer.times = Math.ceil(T / bouncer.duration);
		bouncer.shouldStop = false;
	}
	else {
		ease = Math.abs(Math.sin(Math.PI * T / bouncer.duration));
		bouncer.now = computeNextInt (bouncer.from, bouncer.to, ease);
	}

	bouncer.firstElement.style.backgroundPositionY = bouncer.now + "px";
}

function bounceDuckStop()
{
	// Simply signals that this is the last bounce
	bouncer.shouldStop = true;
}

function bounceDuckStart(times) 
{
	if (bouncer.timer) {
		clearInterval (bouncer.timer);
		bouncer.timer = null;
	}

	var starttime = (new Date).getTime() - 11;

	bouncer.shouldStop = false;
	bouncer.duration = 650;
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
	configured = 
		(document.getElementById("server").value != null
		&& document.getElementById("server").value != ""
		&& document.getElementById("user").value != null
		&& document.getElementById("user").value != "");
	if(configured) {
		document.getElementById("serverlabel").innerHTML = document.getElementById("nickname").value;
	}
	else {
		document.getElementById("serverlabel").innerHTML = CONFIGURE_ME;
	}
	return configured;
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
		var droppedfilesURI = uri.split("\n");
		for(var i = 0; i < droppedfilesURI.length; i++) {
			droppedfilesURI[i] = droppedfilesURI[i].toPosixPath();
		}
		var droppedfilesLocal = droppedfilesURI.join(" ");
		transfer(droppedfilesLocal);
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

String.prototype.toPosixPath = function()
{
	var tmp = unescape(this.substr(this.indexOf("localhost") + 9));
	return tmp.replace(/([ \[\]\(\)\$&%:=\?\!])/g, "\\$1");
}