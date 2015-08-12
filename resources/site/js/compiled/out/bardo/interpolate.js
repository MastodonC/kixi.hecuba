// Compiled by ClojureScript 0.0-3297 {}
goog.provide('bardo.interpolate');
goog.require('cljs.core');
goog.require('cljs.core.match');
goog.require('clojure.set');
goog.require('bardo.ease');

bardo.interpolate.IFresh = (function (){var obj41148 = {};
return obj41148;
})();

bardo.interpolate.fresh = (function bardo$interpolate$fresh(x){
if((function (){var and__16057__auto__ = x;
if(and__16057__auto__){
return x.bardo$interpolate$IFresh$fresh$arity$1;
} else {
return and__16057__auto__;
}
})()){
return x.bardo$interpolate$IFresh$fresh$arity$1(x);
} else {
var x__16705__auto__ = (((x == null))?null:x);
return (function (){var or__16069__auto__ = (bardo.interpolate.fresh[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (bardo.interpolate.fresh["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"IFresh.fresh",x);
}
}
})().call(null,x);
}
});

(bardo.interpolate.IFresh["number"] = true);

(bardo.interpolate.fresh["number"] = (function (x){
return (0);
}));

cljs.core.List.prototype.bardo$interpolate$IFresh$ = true;

cljs.core.List.prototype.bardo$interpolate$IFresh$fresh$arity$1 = (function (x){
var x__$1 = this;
return cljs.core.List.EMPTY;
});

cljs.core.PersistentArrayMap.prototype.bardo$interpolate$IFresh$ = true;

cljs.core.PersistentArrayMap.prototype.bardo$interpolate$IFresh$fresh$arity$1 = (function (x){
var x__$1 = this;
return cljs.core.PersistentArrayMap.EMPTY;
});
bardo.interpolate.hash_map_QMARK_ = cljs.core.every_pred.call(null,cljs.core.coll_QMARK_,cljs.core.complement.call(null,cljs.core.sequential_QMARK_));
/**
 * if a value is nil, replace it with a fresh value of the other
 * value if it satisfies IFresh
 */
bardo.interpolate.wrap_nil = (function bardo$interpolate$wrap_nil(start,end){
try{if((start === null)){
try{if((end === null)){
return null;
} else {
throw cljs.core.match.backtrack;

}
}catch (e41166){if((e41166 instanceof Error)){
var e__27429__auto__ = e41166;
if((e__27429__auto__ === cljs.core.match.backtrack)){
try{if(cljs.core._EQ_.call(null,end,end)){
if((function (){var G__41168 = end;
if(G__41168){
var bit__16743__auto__ = null;
if(cljs.core.truth_((function (){var or__16069__auto__ = bit__16743__auto__;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return G__41168.bardo$interpolate$IFresh$;
}
})())){
return true;
} else {
if((!G__41168.cljs$lang$protocol_mask$partition$)){
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IFresh,G__41168);
} else {
return false;
}
}
} else {
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IFresh,G__41168);
}
})()){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [bardo.interpolate.fresh.call(null,end),end], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [null,end], null);
}
} else {
throw cljs.core.match.backtrack;

}
}catch (e41167){if((e41167 instanceof Error)){
var e__27429__auto____$1 = e41167;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto____$1;
}
} else {
throw e41167;

}
}} else {
throw e__27429__auto__;
}
} else {
throw e41166;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41161){if((e41161 instanceof Error)){
var e__27429__auto__ = e41161;
if((e__27429__auto__ === cljs.core.match.backtrack)){
try{if(cljs.core._EQ_.call(null,start,start)){
try{if((end === null)){
if((function (){var G__41165 = start;
if(G__41165){
var bit__16743__auto__ = null;
if(cljs.core.truth_((function (){var or__16069__auto__ = bit__16743__auto__;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return G__41165.bardo$interpolate$IFresh$;
}
})())){
return true;
} else {
if((!G__41165.cljs$lang$protocol_mask$partition$)){
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IFresh,G__41165);
} else {
return false;
}
}
} else {
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IFresh,G__41165);
}
})()){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start,bardo.interpolate.fresh.call(null,start)], null);
} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start,null], null);
}
} else {
throw cljs.core.match.backtrack;

}
}catch (e41163){if((e41163 instanceof Error)){
var e__27429__auto____$1 = e41163;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
try{if(cljs.core._EQ_.call(null,end,end)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start,end], null);
} else {
throw cljs.core.match.backtrack;

}
}catch (e41164){if((e41164 instanceof Error)){
var e__27429__auto____$2 = e41164;
if((e__27429__auto____$2 === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto____$2;
}
} else {
throw e41164;

}
}} else {
throw e__27429__auto____$1;
}
} else {
throw e41163;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41162){if((e41162 instanceof Error)){
var e__27429__auto____$1 = e41162;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(start),cljs.core.str(" "),cljs.core.str(end)].join('')));
} else {
throw e__27429__auto____$1;
}
} else {
throw e41162;

}
}} else {
throw e__27429__auto__;
}
} else {
throw e41161;

}
}});
bardo.interpolate.wrap_infinite = (function bardo$interpolate$wrap_infinite(x,y){
if(cljs.core.every_QMARK_.call(null,cljs.core.sequential_QMARK_,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null))){
var ocr_41181 = cljs.core.mapv.call(null,cljs.core.counted_QMARK_,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null));
try{if((cljs.core.vector_QMARK_.call(null,ocr_41181)) && ((cljs.core.count.call(null,ocr_41181) === 2))){
try{var ocr_41181_0__41187 = cljs.core.nth.call(null,ocr_41181,(0));
if((ocr_41181_0__41187 === false)){
try{var ocr_41181_1__41188 = cljs.core.nth.call(null,ocr_41181,(1));
if((ocr_41181_1__41188 === false)){
throw Error(Exception,"Cannot interpolate between two uncounted sequences");
} else {
throw cljs.core.match.backtrack;

}
}catch (e41192){if((e41192 instanceof Error)){
var e__27429__auto__ = e41192;
if((e__27429__auto__ === cljs.core.match.backtrack)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.take.call(null,cljs.core.count.call(null,y),x),y], null);
} else {
throw e__27429__auto__;
}
} else {
throw e41192;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41190){if((e41190 instanceof Error)){
var e__27429__auto__ = e41190;
if((e__27429__auto__ === cljs.core.match.backtrack)){
try{var ocr_41181_1__41188 = cljs.core.nth.call(null,ocr_41181,(1));
if((ocr_41181_1__41188 === false)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,cljs.core.take.call(null,cljs.core.count.call(null,x),y)], null);
} else {
throw cljs.core.match.backtrack;

}
}catch (e41191){if((e41191 instanceof Error)){
var e__27429__auto____$1 = e41191;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null);
} else {
throw e__27429__auto____$1;
}
} else {
throw e41191;

}
}} else {
throw e__27429__auto__;
}
} else {
throw e41190;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41189){if((e41189 instanceof Error)){
var e__27429__auto__ = e41189;
if((e__27429__auto__ === cljs.core.match.backtrack)){
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(ocr_41181)].join('')));
} else {
throw e__27429__auto__;
}
} else {
throw e41189;

}
}} else {
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null);
}
});
bardo.interpolate.juxt_args = (function bardo$interpolate$juxt_args(){
var argseq__17109__auto__ = ((((0) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(0)),(0))):null);
return bardo.interpolate.juxt_args.cljs$core$IFn$_invoke$arity$variadic(argseq__17109__auto__);
});

bardo.interpolate.juxt_args.cljs$core$IFn$_invoke$arity$variadic = (function (fns){
return (function() { 
var G__41194__delegate = function (args){
return cljs.core.map_indexed.call(null,(function (idx,f){
return f.call(null,cljs.core.nth.call(null,args,idx,null));
}),fns);
};
var G__41194 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__41195__i = 0, G__41195__a = new Array(arguments.length -  0);
while (G__41195__i < G__41195__a.length) {G__41195__a[G__41195__i] = arguments[G__41195__i + 0]; ++G__41195__i;}
  args = new cljs.core.IndexedSeq(G__41195__a,0);
} 
return G__41194__delegate.call(this,args);};
G__41194.cljs$lang$maxFixedArity = 0;
G__41194.cljs$lang$applyTo = (function (arglist__41196){
var args = cljs.core.seq(arglist__41196);
return G__41194__delegate(args);
});
G__41194.cljs$core$IFn$_invoke$arity$variadic = G__41194__delegate;
return G__41194;
})()
;
});

bardo.interpolate.juxt_args.cljs$lang$maxFixedArity = (0);

bardo.interpolate.juxt_args.cljs$lang$applyTo = (function (seq41193){
return bardo.interpolate.juxt_args.cljs$core$IFn$_invoke$arity$variadic(cljs.core.seq.call(null,seq41193));
});
/**
 * calls (f x y) (f y x) and returns [x y] where f is a function (f x y) that returns [x y]
 */
bardo.interpolate.symmetrical_error = (function bardo$interpolate$symmetrical_error(s,msg,f){
if(cljs.core.truth_((function (){var or__16069__auto__ = cljs.core.apply.call(null,f,s);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.apply.call(null,f,cljs.core.reverse.call(null,s));
}
})())){
throw Error(msg);
} else {
return null;
}
});
bardo.interpolate.pair_pred = (function bardo$interpolate$pair_pred(pred){
return cljs.core.comp.call(null,cljs.core.partial.call(null,cljs.core.every_QMARK_,cljs.core.identity),bardo.interpolate.juxt_args.call(null,pred,cljs.core.complement.call(null,pred)));
});
/**
 * throw appropriate errors if you can't interpolate between two values
 */
bardo.interpolate.wrap_errors = (function bardo$interpolate$wrap_errors(x,y){
var types = new cljs.core.PersistentArrayMap(null, 3, ["seq",cljs.core.sequential_QMARK_,"hash-map",bardo.interpolate.hash_map_QMARK_,"number",cljs.core.number_QMARK_], null);
var seq__41203_41209 = cljs.core.seq.call(null,types);
var chunk__41204_41210 = null;
var count__41205_41211 = (0);
var i__41206_41212 = (0);
while(true){
if((i__41206_41212 < count__41205_41211)){
var vec__41207_41213 = cljs.core._nth.call(null,chunk__41204_41210,i__41206_41212);
var type_41214 = cljs.core.nth.call(null,vec__41207_41213,(0),null);
var pred_41215 = cljs.core.nth.call(null,vec__41207_41213,(1),null);
bardo.interpolate.symmetrical_error.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null),[cljs.core.str("Cannot interpolate between a "),cljs.core.str(type_41214),cljs.core.str(" and something else")].join(''),bardo.interpolate.pair_pred.call(null,pred_41215));

var G__41216 = seq__41203_41209;
var G__41217 = chunk__41204_41210;
var G__41218 = count__41205_41211;
var G__41219 = (i__41206_41212 + (1));
seq__41203_41209 = G__41216;
chunk__41204_41210 = G__41217;
count__41205_41211 = G__41218;
i__41206_41212 = G__41219;
continue;
} else {
var temp__4425__auto___41220 = cljs.core.seq.call(null,seq__41203_41209);
if(temp__4425__auto___41220){
var seq__41203_41221__$1 = temp__4425__auto___41220;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__41203_41221__$1)){
var c__16854__auto___41222 = cljs.core.chunk_first.call(null,seq__41203_41221__$1);
var G__41223 = cljs.core.chunk_rest.call(null,seq__41203_41221__$1);
var G__41224 = c__16854__auto___41222;
var G__41225 = cljs.core.count.call(null,c__16854__auto___41222);
var G__41226 = (0);
seq__41203_41209 = G__41223;
chunk__41204_41210 = G__41224;
count__41205_41211 = G__41225;
i__41206_41212 = G__41226;
continue;
} else {
var vec__41208_41227 = cljs.core.first.call(null,seq__41203_41221__$1);
var type_41228 = cljs.core.nth.call(null,vec__41208_41227,(0),null);
var pred_41229 = cljs.core.nth.call(null,vec__41208_41227,(1),null);
bardo.interpolate.symmetrical_error.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null),[cljs.core.str("Cannot interpolate between a "),cljs.core.str(type_41228),cljs.core.str(" and something else")].join(''),bardo.interpolate.pair_pred.call(null,pred_41229));

var G__41230 = cljs.core.next.call(null,seq__41203_41221__$1);
var G__41231 = null;
var G__41232 = (0);
var G__41233 = (0);
seq__41203_41209 = G__41230;
chunk__41204_41210 = G__41231;
count__41205_41211 = G__41232;
i__41206_41212 = G__41233;
continue;
}
} else {
}
}
break;
}

return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null);
});
/**
 * removed keys not present in start or end of interpolation
 */
bardo.interpolate.wrap_size = (function bardo$interpolate$wrap_size(start,end){
return (function (intrpl){
return (function (t){
var v = intrpl.call(null,t);
try{if((t === (0))){
try{if(cljs.core.truth_(bardo.interpolate.hash_map_QMARK_.call(null,v))){
return cljs.core.select_keys.call(null,v,cljs.core.keys.call(null,start));
} else {
throw cljs.core.match.backtrack;

}
}catch (e41265){if((e41265 instanceof Error)){
var e__27429__auto__ = e41265;
if((e__27429__auto__ === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto__;
}
} else {
throw e41265;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41258){if((e41258 instanceof Error)){
var e__27429__auto__ = e41258;
if((e__27429__auto__ === cljs.core.match.backtrack)){
try{if((t === (1))){
try{if(cljs.core.truth_(bardo.interpolate.hash_map_QMARK_.call(null,v))){
return cljs.core.select_keys.call(null,v,cljs.core.keys.call(null,end));
} else {
throw cljs.core.match.backtrack;

}
}catch (e41264){if((e41264 instanceof Error)){
var e__27429__auto____$1 = e41264;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto____$1;
}
} else {
throw e41264;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41259){if((e41259 instanceof Error)){
var e__27429__auto____$1 = e41259;
if((e__27429__auto____$1 === cljs.core.match.backtrack)){
try{if((t === (0))){
try{if(cljs.core.sequential_QMARK_.call(null,v)){
return cljs.core.vec.call(null,cljs.core.take.call(null,cljs.core.count.call(null,start),v));
} else {
throw cljs.core.match.backtrack;

}
}catch (e41263){if((e41263 instanceof Error)){
var e__27429__auto____$2 = e41263;
if((e__27429__auto____$2 === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto____$2;
}
} else {
throw e41263;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41260){if((e41260 instanceof Error)){
var e__27429__auto____$2 = e41260;
if((e__27429__auto____$2 === cljs.core.match.backtrack)){
try{if((t === (1))){
try{if(cljs.core.sequential_QMARK_.call(null,v)){
return cljs.core.vec.call(null,cljs.core.take.call(null,cljs.core.count.call(null,end),v));
} else {
throw cljs.core.match.backtrack;

}
}catch (e41262){if((e41262 instanceof Error)){
var e__27429__auto____$3 = e41262;
if((e__27429__auto____$3 === cljs.core.match.backtrack)){
throw cljs.core.match.backtrack;
} else {
throw e__27429__auto____$3;
}
} else {
throw e41262;

}
}} else {
throw cljs.core.match.backtrack;

}
}catch (e41261){if((e41261 instanceof Error)){
var e__27429__auto____$3 = e41261;
if((e__27429__auto____$3 === cljs.core.match.backtrack)){
return v;
} else {
throw e__27429__auto____$3;
}
} else {
throw e41261;

}
}} else {
throw e__27429__auto____$2;
}
} else {
throw e41260;

}
}} else {
throw e__27429__auto____$1;
}
} else {
throw e41259;

}
}} else {
throw e__27429__auto__;
}
} else {
throw e41258;

}
}});
});
});

bardo.interpolate.IInterpolate = (function (){var obj41267 = {};
return obj41267;
})();

bardo.interpolate._interpolate = (function bardo$interpolate$_interpolate(start,end){
if((function (){var and__16057__auto__ = start;
if(and__16057__auto__){
return start.bardo$interpolate$IInterpolate$_interpolate$arity$2;
} else {
return and__16057__auto__;
}
})()){
return start.bardo$interpolate$IInterpolate$_interpolate$arity$2(start,end);
} else {
var x__16705__auto__ = (((start == null))?null:start);
return (function (){var or__16069__auto__ = (bardo.interpolate._interpolate[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (bardo.interpolate._interpolate["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"IInterpolate.-interpolate",start);
}
}
})().call(null,start,end);
}
});

(bardo.interpolate.IInterpolate["number"] = true);

(bardo.interpolate._interpolate["number"] = (function (start,end){
return (function (t){
return (start + (t * (end - start)));
});
}));

cljs.core.List.prototype.bardo$interpolate$IInterpolate$ = true;

cljs.core.List.prototype.bardo$interpolate$IInterpolate$_interpolate$arity$2 = (function (start,end){
var start__$1 = this;
return ((function (start__$1){
return (function (t){
return cljs.core.into.call(null,cljs.core.PersistentVector.EMPTY,(function (){var iter__16823__auto__ = ((function (start__$1){
return (function bardo$interpolate$iter__41272(s__41273){
return (new cljs.core.LazySeq(null,((function (start__$1){
return (function (){
var s__41273__$1 = s__41273;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__41273__$1);
if(temp__4425__auto__){
var s__41273__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__41273__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__41273__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__41275 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__41274 = (0);
while(true){
if((i__41274 < size__16822__auto__)){
var k = cljs.core._nth.call(null,c__16821__auto__,i__41274);
cljs.core.chunk_append.call(null,b__41275,((function (i__41274,k,c__16821__auto__,size__16822__auto__,b__41275,s__41273__$2,temp__4425__auto__,start__$1){
return (function (p1__41270_SHARP_){
return p1__41270_SHARP_.call(null,t);
});})(i__41274,k,c__16821__auto__,size__16822__auto__,b__41275,s__41273__$2,temp__4425__auto__,start__$1))
.call(null,cljs.core.apply.call(null,bardo.interpolate.interpolate,cljs.core.apply.call(null,bardo.interpolate.wrap_nil,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.nth.call(null,start__$1,k,null),cljs.core.nth.call(null,end,k,null)], null)))));

var G__41280 = (i__41274 + (1));
i__41274 = G__41280;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__41275),bardo$interpolate$iter__41272.call(null,cljs.core.chunk_rest.call(null,s__41273__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__41275),null);
}
} else {
var k = cljs.core.first.call(null,s__41273__$2);
return cljs.core.cons.call(null,((function (k,s__41273__$2,temp__4425__auto__,start__$1){
return (function (p1__41270_SHARP_){
return p1__41270_SHARP_.call(null,t);
});})(k,s__41273__$2,temp__4425__auto__,start__$1))
.call(null,cljs.core.apply.call(null,bardo.interpolate.interpolate,cljs.core.apply.call(null,bardo.interpolate.wrap_nil,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.nth.call(null,start__$1,k,null),cljs.core.nth.call(null,end,k,null)], null)))),bardo$interpolate$iter__41272.call(null,cljs.core.rest.call(null,s__41273__$2)));
}
} else {
return null;
}
break;
}
});})(start__$1))
,null,null));
});})(start__$1))
;
return iter__16823__auto__.call(null,cljs.core.range.call(null,Math.max(cljs.core.count.call(null,start__$1),cljs.core.count.call(null,end))));
})());
});
;})(start__$1))
});

cljs.core.PersistentArrayMap.prototype.bardo$interpolate$IInterpolate$ = true;

cljs.core.PersistentArrayMap.prototype.bardo$interpolate$IInterpolate$_interpolate$arity$2 = (function (start,end){
var start__$1 = this;
return ((function (start__$1){
return (function (t){
return cljs.core.into.call(null,cljs.core.PersistentArrayMap.EMPTY,(function (){var iter__16823__auto__ = ((function (start__$1){
return (function bardo$interpolate$iter__41276(s__41277){
return (new cljs.core.LazySeq(null,((function (start__$1){
return (function (){
var s__41277__$1 = s__41277;
while(true){
var temp__4425__auto__ = cljs.core.seq.call(null,s__41277__$1);
if(temp__4425__auto__){
var s__41277__$2 = temp__4425__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,s__41277__$2)){
var c__16821__auto__ = cljs.core.chunk_first.call(null,s__41277__$2);
var size__16822__auto__ = cljs.core.count.call(null,c__16821__auto__);
var b__41279 = cljs.core.chunk_buffer.call(null,size__16822__auto__);
if((function (){var i__41278 = (0);
while(true){
if((i__41278 < size__16822__auto__)){
var k = cljs.core._nth.call(null,c__16821__auto__,i__41278);
cljs.core.chunk_append.call(null,b__41279,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [k,((function (i__41278,k,c__16821__auto__,size__16822__auto__,b__41279,s__41277__$2,temp__4425__auto__,start__$1){
return (function (p1__41271_SHARP_){
return p1__41271_SHARP_.call(null,t);
});})(i__41278,k,c__16821__auto__,size__16822__auto__,b__41279,s__41277__$2,temp__4425__auto__,start__$1))
.call(null,cljs.core.apply.call(null,bardo.interpolate.interpolate,cljs.core.map.call(null,k,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start__$1,end], null))))], null));

var G__41281 = (i__41278 + (1));
i__41278 = G__41281;
continue;
} else {
return true;
}
break;
}
})()){
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__41279),bardo$interpolate$iter__41276.call(null,cljs.core.chunk_rest.call(null,s__41277__$2)));
} else {
return cljs.core.chunk_cons.call(null,cljs.core.chunk.call(null,b__41279),null);
}
} else {
var k = cljs.core.first.call(null,s__41277__$2);
return cljs.core.cons.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [k,((function (k,s__41277__$2,temp__4425__auto__,start__$1){
return (function (p1__41271_SHARP_){
return p1__41271_SHARP_.call(null,t);
});})(k,s__41277__$2,temp__4425__auto__,start__$1))
.call(null,cljs.core.apply.call(null,bardo.interpolate.interpolate,cljs.core.map.call(null,k,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start__$1,end], null))))], null),bardo$interpolate$iter__41276.call(null,cljs.core.rest.call(null,s__41277__$2)));
}
} else {
return null;
}
break;
}
});})(start__$1))
,null,null));
});})(start__$1))
;
return iter__16823__auto__.call(null,cljs.core.apply.call(null,clojure.set.union,cljs.core.map.call(null,cljs.core.set,cljs.core.map.call(null,cljs.core.keys,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start__$1,end], null)))));
})());
});
;})(start__$1))
});
bardo.interpolate.interpolate = (function bardo$interpolate$interpolate(start,end){
var wrapped = (function (){var G__41285 = new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [start,end], null);
var G__41285__$1 = (((G__41285 == null))?null:cljs.core.apply.call(null,bardo.interpolate.wrap_nil,G__41285));
var G__41285__$2 = (((G__41285__$1 == null))?null:cljs.core.apply.call(null,bardo.interpolate.wrap_errors,G__41285__$1));
var G__41285__$3 = (((G__41285__$2 == null))?null:cljs.core.apply.call(null,bardo.interpolate.wrap_infinite,G__41285__$2));
return G__41285__$3;
})();
var can_interpolate = cljs.core.mapv.call(null,((function (wrapped){
return (function (p1__41282_SHARP_){
var G__41286 = p1__41282_SHARP_;
if(G__41286){
var bit__16743__auto__ = null;
if(cljs.core.truth_((function (){var or__16069__auto__ = bit__16743__auto__;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return G__41286.bardo$interpolate$IInterpolate$;
}
})())){
return true;
} else {
if((!G__41286.cljs$lang$protocol_mask$partition$)){
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IInterpolate,G__41286);
} else {
return false;
}
}
} else {
return cljs.core.native_satisfies_QMARK_.call(null,bardo.interpolate.IInterpolate,G__41286);
}
});})(wrapped))
,wrapped);
if(cljs.core.truth_(cljs.core.apply.call(null,cljs.core._EQ_,true,can_interpolate))){
return cljs.core.apply.call(null,bardo.interpolate.wrap_size,wrapped).call(null,cljs.core.apply.call(null,bardo.interpolate._interpolate,wrapped));
} else {
throw Error();
}
});
bardo.interpolate.into_lazy_seq = (function bardo$interpolate$into_lazy_seq(intrpl,vals){
if(cljs.core.seq.call(null,cljs.core.rest.call(null,vals))){
return cljs.core.cons.call(null,intrpl.call(null,cljs.core.first.call(null,vals)),(new cljs.core.LazySeq(null,(function (){
return bardo$interpolate$into_lazy_seq.call(null,intrpl,cljs.core.rest.call(null,vals));
}),null,null)));
} else {
return (new cljs.core.PersistentVector(null,1,(5),cljs.core.PersistentVector.EMPTY_NODE,[intrpl.call(null,cljs.core.first.call(null,vals))],null));
}
});
bardo.interpolate.mix = (function bardo$interpolate$mix(start,end){
return (function (t){
return bardo.interpolate.interpolate.call(null,start.call(null,t),end.call(null,t)).call(null,t);
});
});
bardo.interpolate.blend = (function bardo$interpolate$blend(intrpl,end){
return (function (t){
return bardo.interpolate.interpolate.call(null,intrpl.call(null,t),end).call(null,t);
});
});
bardo.interpolate.chain = (function bardo$interpolate$chain(){
var G__41288 = arguments.length;
switch (G__41288) {
case 2:
return bardo.interpolate.chain.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return bardo.interpolate.chain.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

bardo.interpolate.chain.cljs$core$IFn$_invoke$arity$2 = (function (intrpl,end){
return bardo.interpolate.chain.call(null,intrpl,end,0.5);
});

bardo.interpolate.chain.cljs$core$IFn$_invoke$arity$3 = (function (intrpl,end,mid){
var start = bardo.ease.shift.call(null,intrpl,(0),mid);
var end__$1 = bardo.ease.shift.call(null,bardo.interpolate.interpolate.call(null,intrpl.call(null,(1)),end),mid,(1));
return ((function (start,end__$1){
return (function (t){
if((t < mid)){
return start.call(null,t);
} else {
if((t >= mid)){
return end__$1.call(null,t);
} else {
return null;
}
}
});
;})(start,end__$1))
});

bardo.interpolate.chain.cljs$lang$maxFixedArity = 3;
bardo.interpolate.pipeline = (function bardo$interpolate$pipeline(){
var G__41292 = arguments.length;
switch (G__41292) {
case 1:
return bardo.interpolate.pipeline.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return bardo.interpolate.pipeline.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

bardo.interpolate.pipeline.cljs$core$IFn$_invoke$arity$1 = (function (states){
var n = cljs.core.count.call(null,states);
return bardo.interpolate.pipeline.call(null,states,cljs.core.cons.call(null,(0),cljs.core.map.call(null,cljs.core.partial.call(null,cljs.core._STAR_,((1) / (n - (1)))),cljs.core.range.call(null,(1),n))));
});

bardo.interpolate.pipeline.cljs$core$IFn$_invoke$arity$2 = (function (states,input){
var n = cljs.core.count.call(null,states);
var vec__41293 = states;
var start = cljs.core.nth.call(null,vec__41293,(0),null);
var second = cljs.core.nth.call(null,vec__41293,(1),null);
var states__$1 = cljs.core.nthnext.call(null,vec__41293,(2));
var output = cljs.core.cons.call(null,(0),cljs.core.reverse.call(null,cljs.core.take.call(null,(n - (1)),cljs.core.iterate.call(null,((function (n,vec__41293,start,second,states__$1){
return (function (p1__41290_SHARP_){
return (p1__41290_SHARP_ / (2));
});})(n,vec__41293,start,second,states__$1))
,(1)))));
return bardo.ease.shift_parts.call(null,cljs.core.reduce.call(null,bardo.interpolate.chain,bardo.interpolate.interpolate.call(null,start,second),states__$1),input,output);
});

bardo.interpolate.pipeline.cljs$lang$maxFixedArity = 2;

//# sourceMappingURL=interpolate.js.map?rel=1439206058529