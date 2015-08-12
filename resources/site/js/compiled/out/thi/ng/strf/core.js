// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.strf.core');
goog.require('cljs.core');
goog.require('clojure.string');
thi.ng.strf.core.parse_int = (function thi$ng$strf$core$parse_int(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.strf.core.parse_int.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.strf.core.parse_int.cljs$core$IFn$_invoke$arity$variadic = (function (x,radix,p__30954){
var vec__30955 = p__30954;
var default$ = cljs.core.nth.call(null,vec__30955,(0),null);
var x_SINGLEQUOTE_ = parseInt(x,radix);
if(cljs.core.truth_(isNaN(x_SINGLEQUOTE_))){
return default$;
} else {
return x_SINGLEQUOTE_;
}
});

thi.ng.strf.core.parse_int.cljs$lang$maxFixedArity = (2);

thi.ng.strf.core.parse_int.cljs$lang$applyTo = (function (seq30951){
var G__30952 = cljs.core.first.call(null,seq30951);
var seq30951__$1 = cljs.core.next.call(null,seq30951);
var G__30953 = cljs.core.first.call(null,seq30951__$1);
var seq30951__$2 = cljs.core.next.call(null,seq30951__$1);
return thi.ng.strf.core.parse_int.cljs$core$IFn$_invoke$arity$variadic(G__30952,G__30953,seq30951__$2);
});
thi.ng.strf.core.parse_long = (function thi$ng$strf$core$parse_long(){
var argseq__17109__auto__ = ((((2) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(2)),(0))):null);
return thi.ng.strf.core.parse_long.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__17109__auto__);
});

thi.ng.strf.core.parse_long.cljs$core$IFn$_invoke$arity$variadic = (function (x,radix,p__30959){
var vec__30960 = p__30959;
var default$ = cljs.core.nth.call(null,vec__30960,(0),null);
return thi.ng.strf.core.parse_int.call(null,x,radix,default$);
});

thi.ng.strf.core.parse_long.cljs$lang$maxFixedArity = (2);

thi.ng.strf.core.parse_long.cljs$lang$applyTo = (function (seq30956){
var G__30957 = cljs.core.first.call(null,seq30956);
var seq30956__$1 = cljs.core.next.call(null,seq30956);
var G__30958 = cljs.core.first.call(null,seq30956__$1);
var seq30956__$2 = cljs.core.next.call(null,seq30956__$1);
return thi.ng.strf.core.parse_long.cljs$core$IFn$_invoke$arity$variadic(G__30957,G__30958,seq30956__$2);
});
thi.ng.strf.core.parse_float = (function thi$ng$strf$core$parse_float(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.strf.core.parse_float.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.strf.core.parse_float.cljs$core$IFn$_invoke$arity$variadic = (function (x,p__30963){
var vec__30964 = p__30963;
var default$ = cljs.core.nth.call(null,vec__30964,(0),null);
var x_SINGLEQUOTE_ = parseFloat(x);
if(cljs.core.truth_(isNaN(x_SINGLEQUOTE_))){
return default$;
} else {
return x_SINGLEQUOTE_;
}
});

thi.ng.strf.core.parse_float.cljs$lang$maxFixedArity = (1);

thi.ng.strf.core.parse_float.cljs$lang$applyTo = (function (seq30961){
var G__30962 = cljs.core.first.call(null,seq30961);
var seq30961__$1 = cljs.core.next.call(null,seq30961);
return thi.ng.strf.core.parse_float.cljs$core$IFn$_invoke$arity$variadic(G__30962,seq30961__$1);
});
thi.ng.strf.core.parse_double = (function thi$ng$strf$core$parse_double(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.strf.core.parse_double.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.strf.core.parse_double.cljs$core$IFn$_invoke$arity$variadic = (function (x,p__30967){
var vec__30968 = p__30967;
var default$ = cljs.core.nth.call(null,vec__30968,(0),null);
return thi.ng.strf.core.parse_float.call(null,x,default$);
});

thi.ng.strf.core.parse_double.cljs$lang$maxFixedArity = (1);

thi.ng.strf.core.parse_double.cljs$lang$applyTo = (function (seq30965){
var G__30966 = cljs.core.first.call(null,seq30965);
var seq30965__$1 = cljs.core.next.call(null,seq30965);
return thi.ng.strf.core.parse_double.cljs$core$IFn$_invoke$arity$variadic(G__30966,seq30965__$1);
});
thi.ng.strf.core.parse_boolean = (function thi$ng$strf$core$parse_boolean(x){
return cljs.core._EQ_.call(null,"true",clojure.string.lower_case.call(null,x));
});
thi.ng.strf.core.float$ = cljs.core.memoize.call(null,(function (prec){
return (function (x){
return (new Number(x)).toFixed(prec);
});
}));
thi.ng.strf.core.pad_left = cljs.core.memoize.call(null,(function (len,fill){
var fill__$1 = cljs.core.apply.call(null,cljs.core.str,cljs.core.repeat.call(null,len,fill));
return ((function (fill__$1){
return (function (x){
var l = cljs.core.count.call(null,x);
if((l < len)){
return [cljs.core.str(cljs.core.subs.call(null,fill__$1,(0),(len - l))),cljs.core.str(x)].join('');
} else {
return x;
}
});
;})(fill__$1))
}));
thi.ng.strf.core.pad_right = cljs.core.memoize.call(null,(function (len,fill){
var fill__$1 = cljs.core.apply.call(null,cljs.core.str,cljs.core.repeat.call(null,len,fill));
return ((function (fill__$1){
return (function (x){
var l = cljs.core.count.call(null,x);
if((l < len)){
return [cljs.core.str(x),cljs.core.str(cljs.core.subs.call(null,fill__$1,(0),(len - l)))].join('');
} else {
return x;
}
});
;})(fill__$1))
}));
thi.ng.strf.core.hex = cljs.core.memoize.call(null,(function (len){
var pad = thi.ng.strf.core.pad_left.call(null,len,"0");
return ((function (pad){
return (function (x){
return pad.call(null,(new Number((x | (0)))).toString((16)));
});
;})(pad))
}));
thi.ng.strf.core.trunc_left = (function thi$ng$strf$core$trunc_left(len){
return (function (x){
var l = cljs.core.count.call(null,x);
if((l <= len)){
return x;
} else {
return cljs.core.subs.call(null,x,(0),len);
}
});
});
thi.ng.strf.core.trunc_right = (function thi$ng$strf$core$trunc_right(len){
return (function (x){
var l = cljs.core.count.call(null,x);
if((l <= len)){
return x;
} else {
return cljs.core.subs.call(null,x,(l - len));
}
});
});
thi.ng.strf.core.format = (function thi$ng$strf$core$format(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return thi.ng.strf.core.format.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

thi.ng.strf.core.format.cljs$core$IFn$_invoke$arity$variadic = (function (fmt,args){
var s = cljs.core.PersistentVector.EMPTY;
var fmt__$1 = fmt;
var args__$1 = args;
while(true){
var temp__4423__auto__ = cljs.core.first.call(null,fmt__$1);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
if(typeof f === 'string'){
var G__30971 = cljs.core.conj.call(null,s,f);
var G__30972 = cljs.core.next.call(null,fmt__$1);
var G__30973 = args__$1;
s = G__30971;
fmt__$1 = G__30972;
args__$1 = G__30973;
continue;
} else {
var G__30974 = cljs.core.conj.call(null,s,f.call(null,cljs.core.first.call(null,args__$1)));
var G__30975 = cljs.core.next.call(null,fmt__$1);
var G__30976 = cljs.core.next.call(null,args__$1);
s = G__30974;
fmt__$1 = G__30975;
args__$1 = G__30976;
continue;
}
} else {
return cljs.core.apply.call(null,cljs.core.str,s);
}
break;
}
});

thi.ng.strf.core.format.cljs$lang$maxFixedArity = (1);

thi.ng.strf.core.format.cljs$lang$applyTo = (function (seq30969){
var G__30970 = cljs.core.first.call(null,seq30969);
var seq30969__$1 = cljs.core.next.call(null,seq30969);
return thi.ng.strf.core.format.cljs$core$IFn$_invoke$arity$variadic(G__30970,seq30969__$1);
});
thi.ng.strf.core.date_formatters = (function (){var d2 = cljs.core.comp.call(null,thi.ng.strf.core.pad_left.call(null,(2),"0"),cljs.core.str);
return new cljs.core.PersistentArrayMap(null, 6, [new cljs.core.Keyword(null,"yyyy-mm-dd","yyyy-mm-dd",1870000488),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.str,"-",d2,"-",d2], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [d.getFullYear(),(d.getMonth() + (1)),d.getDate()], null));
});})(d2))
,new cljs.core.Keyword(null,"yyyy-mm-dd-hh-mm-ss","yyyy-mm-dd-hh-mm-ss",515818629),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.str,"-",d2,"-",d2," ",d2,":",d2,":",d2], null),new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [d.getFullYear(),(d.getMonth() + (1)),d.getDate(),d.getHours(),d.getMinutes(),d.getSeconds()], null));
});})(d2))
,new cljs.core.Keyword(null,"dd-mm-yyyy","dd-mm-yyyy",-705729643),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [d2,"/",d2,"/",cljs.core.str], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [d.getDate(),(d.getMonth() + (1)),d.getFullYear()], null));
});})(d2))
,new cljs.core.Keyword(null,"mm-dd-yyyy","mm-dd-yyyy",-911118576),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [d2,"/",d2,"/",cljs.core.str], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [(d.getMonth() + (1)),d.getDate(),d.getFullYear()], null));
});})(d2))
,new cljs.core.Keyword(null,"dd-mm-yy","dd-mm-yy",-664945286),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [d2,"/",d2,"/",cljs.core.comp.call(null,d2,((function (d2){
return (function (p1__30977_SHARP_){
return cljs.core.mod.call(null,p1__30977_SHARP_,(100));
});})(d2))
)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [d.getDate(),(d.getMonth() + (1)),d.getFullYear()], null));
});})(d2))
,new cljs.core.Keyword(null,"hh-mm-ss","hh-mm-ss",1000840174),((function (d2){
return (function (d){
return cljs.core.apply.call(null,thi.ng.strf.core.format,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [d2,":",d2,":",d2], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [d.getHours(),d.getMinutes(),d.getSeconds()], null));
});})(d2))
], null);
})();
thi.ng.strf.core.format_date = (function thi$ng$strf$core$format_date(){
var G__30979 = arguments.length;
switch (G__30979) {
case 1:
return thi.ng.strf.core.format_date.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return thi.ng.strf.core.format_date.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.strf.core.format_date.cljs$core$IFn$_invoke$arity$1 = (function (d){
return thi.ng.strf.core.date_formatters.call(null,new cljs.core.Keyword(null,"yyyy-mm-dd-hh-mm-ss","yyyy-mm-dd-hh-mm-ss",515818629)).call(null,d);
});

thi.ng.strf.core.format_date.cljs$core$IFn$_invoke$arity$2 = (function (fmt,d){
if(cljs.core.fn_QMARK_.call(null,fmt)){
return fmt.call(null,d);
} else {
return thi.ng.strf.core.date_formatters.call(null,fmt).call(null,d);
}
});

thi.ng.strf.core.format_date.cljs$lang$maxFixedArity = 2;
thi.ng.strf.core.now = (function thi$ng$strf$core$now(){
return (new Date());
});
thi.ng.strf.core.timestamp = (function thi$ng$strf$core$timestamp(){
return (new Date()).getTime();
});
thi.ng.strf.core.html_entities = new cljs.core.PersistentArrayMap(null, 4, ["&","&amp;","<","&lt;",">","&gt;","\"","&quot;"], null);
thi.ng.strf.core.as_str = (function thi$ng$strf$core$as_str(x){
if(((x instanceof cljs.core.Keyword)) || ((x instanceof cljs.core.Symbol))){
return cljs.core.name.call(null,x);
} else {
return [cljs.core.str(x)].join('');
}
});
thi.ng.strf.core.escape_html = (function thi$ng$strf$core$escape_html(x){
return clojure.string.escape.call(null,thi.ng.strf.core.as_str.call(null,x),thi.ng.strf.core.html_entities);
});

//# sourceMappingURL=core.js.map?rel=1439206034659