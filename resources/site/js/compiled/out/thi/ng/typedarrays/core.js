// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.typedarrays.core');
goog.require('cljs.core');
/**
 * Creates a native Int8Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.int8 = (function thi$ng$typedarrays$core$int8(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Int8Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Int8Array(len));
var i_32987 = (0);
var coll_32988 = size_or_coll;
while(true){
if((i_32987 < len)){
(buf[i_32987] = cljs.core.first.call(null,coll_32988));

var G__32989 = (i_32987 + (1));
var G__32990 = cljs.core.next.call(null,coll_32988);
i_32987 = G__32989;
coll_32988 = G__32990;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Uint8Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.uint8 = (function thi$ng$typedarrays$core$uint8(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Uint8Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Uint8Array(len));
var i_32991 = (0);
var coll_32992 = size_or_coll;
while(true){
if((i_32991 < len)){
(buf[i_32991] = cljs.core.first.call(null,coll_32992));

var G__32993 = (i_32991 + (1));
var G__32994 = cljs.core.next.call(null,coll_32992);
i_32991 = G__32993;
coll_32992 = G__32994;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Uint8ClampedArray of the given size or from `coll`.
 */
thi.ng.typedarrays.core.uint8_clamped = (function thi$ng$typedarrays$core$uint8_clamped(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Uint8ClampedArray(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Uint8ClampedArray(len));
var i_32995 = (0);
var coll_32996 = size_or_coll;
while(true){
if((i_32995 < len)){
(buf[i_32995] = cljs.core.first.call(null,coll_32996));

var G__32997 = (i_32995 + (1));
var G__32998 = cljs.core.next.call(null,coll_32996);
i_32995 = G__32997;
coll_32996 = G__32998;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Int16Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.int16 = (function thi$ng$typedarrays$core$int16(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Int16Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Int16Array(len));
var i_32999 = (0);
var coll_33000 = size_or_coll;
while(true){
if((i_32999 < len)){
(buf[i_32999] = cljs.core.first.call(null,coll_33000));

var G__33001 = (i_32999 + (1));
var G__33002 = cljs.core.next.call(null,coll_33000);
i_32999 = G__33001;
coll_33000 = G__33002;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Uint16Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.uint16 = (function thi$ng$typedarrays$core$uint16(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Uint16Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Uint16Array(len));
var i_33003 = (0);
var coll_33004 = size_or_coll;
while(true){
if((i_33003 < len)){
(buf[i_33003] = cljs.core.first.call(null,coll_33004));

var G__33005 = (i_33003 + (1));
var G__33006 = cljs.core.next.call(null,coll_33004);
i_33003 = G__33005;
coll_33004 = G__33006;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Int32Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.int32 = (function thi$ng$typedarrays$core$int32(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Int32Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Int32Array(len));
var i_33007 = (0);
var coll_33008 = size_or_coll;
while(true){
if((i_33007 < len)){
(buf[i_33007] = cljs.core.first.call(null,coll_33008));

var G__33009 = (i_33007 + (1));
var G__33010 = cljs.core.next.call(null,coll_33008);
i_33007 = G__33009;
coll_33008 = G__33010;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Uint32Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.uint32 = (function thi$ng$typedarrays$core$uint32(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Uint32Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Uint32Array(len));
var i_33011 = (0);
var coll_33012 = size_or_coll;
while(true){
if((i_33011 < len)){
(buf[i_33011] = cljs.core.first.call(null,coll_33012));

var G__33013 = (i_33011 + (1));
var G__33014 = cljs.core.next.call(null,coll_33012);
i_33011 = G__33013;
coll_33012 = G__33014;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Float32Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.float32 = (function thi$ng$typedarrays$core$float32(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Float32Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Float32Array(len));
var i_33015 = (0);
var coll_33016 = size_or_coll;
while(true){
if((i_33015 < len)){
(buf[i_33015] = cljs.core.first.call(null,coll_33016));

var G__33017 = (i_33015 + (1));
var G__33018 = cljs.core.next.call(null,coll_33016);
i_33015 = G__33017;
coll_33016 = G__33018;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Creates a native Float64Array of the given size or from `coll`.
 */
thi.ng.typedarrays.core.float64 = (function thi$ng$typedarrays$core$float64(size_or_coll){
if(typeof size_or_coll === 'number'){
return (new Float64Array(size_or_coll));
} else {
var len = cljs.core.count.call(null,size_or_coll);
var buf = (new Float64Array(len));
var i_33019 = (0);
var coll_33020 = size_or_coll;
while(true){
if((i_33019 < len)){
(buf[i_33019] = cljs.core.first.call(null,coll_33020));

var G__33021 = (i_33019 + (1));
var G__33022 = cljs.core.next.call(null,coll_33020);
i_33019 = G__33021;
coll_33020 = G__33022;
continue;
} else {
}
break;
}

return buf;
}
});
/**
 * Returns true if JS runtime supports typed arrays
 */
thi.ng.typedarrays.core.typed_arrays_supported_QMARK_ = (function thi$ng$typedarrays$core$typed_arrays_supported_QMARK_(){
return !(((window["ArrayBuffer"]) == null));
});
thi.ng.typedarrays.core.array_types = cljs.core.PersistentHashMap.fromArrays(["[object Float64Array]","[object Int8Array]","[object Int16Array]","[object Uint8Array]","[object Uint16Array]","[object Uint8ClampedArray]","[object Uint32Array]","[object Float32Array]","[object Int32Array]"],[new cljs.core.Keyword(null,"float64","float64",1881838306),new cljs.core.Keyword(null,"int8","int8",-1834023920),new cljs.core.Keyword(null,"int16","int16",-188764863),new cljs.core.Keyword(null,"uint8","uint8",956521151),new cljs.core.Keyword(null,"uint16","uint16",-588869202),new cljs.core.Keyword(null,"uint8-clamped","uint8-clamped",1439331936),new cljs.core.Keyword(null,"uint32","uint32",-418789486),new cljs.core.Keyword(null,"float32","float32",-2119815775),new cljs.core.Keyword(null,"int32","int32",1718804896)]);
thi.ng.typedarrays.core.array_type = (function thi$ng$typedarrays$core$array_type(x){
if(cljs.core.array_QMARK_.call(null,x)){
return new cljs.core.Keyword(null,"array","array",-2080713842);
} else {
return thi.ng.typedarrays.core.array_types.call(null,[cljs.core.str(x)].join(''));
}
});
/**
 * Returns truthy value if the given arg is a typed array instance
 */
thi.ng.typedarrays.core.typed_array_QMARK_ = (function thi$ng$typedarrays$core$typed_array_QMARK_(x){
if(cljs.core.sequential_QMARK_.call(null,x)){
return false;
} else {
if(typeof x === 'number'){
return false;
} else {
return thi.ng.typedarrays.core.array_types.call(null,[cljs.core.str(x)].join(''));

}
}
});

//# sourceMappingURL=core.js.map?rel=1439206040604