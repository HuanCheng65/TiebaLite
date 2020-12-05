/**
 * @name: ClipboardGuard
 * @author: huanchengfly
 **/
"use strict";function _toConsumableArray(e){if(Array.isArray(e)){for(var n=0,r=Array(e.length);n<e.length;n++)r[n]=e[n];return r}return Array.from(e)}function eventHandler(e){console.log(e.path.length),e.path.length>=threshold||confirm("ClipboardGuardCopyRequest")||(e.preventDefault(),e.stopPropagation())}var threshold=7,key=encodeURIComponent("ClipboardGuardRunning");!function(){if(!window[key])try{window[key]=!0,[document].concat(_toConsumableArray(Array.from(document.getElementsByTagName("iframe")).map(function(e){return e.contentDocument}))).forEach(function(e){e.addEventListener("copy",eventHandler,{passive:!1,capture:!0})})}catch(e){console.log("ClipboardGuard: "+e)}}();