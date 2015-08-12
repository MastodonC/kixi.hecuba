// Compiled by ClojureScript 0.0-3297 {}
goog.provide('cljs.core.async');
goog.require('cljs.core');
goog.require('cljs.core.async.impl.channels');
goog.require('cljs.core.async.impl.dispatch');
goog.require('cljs.core.async.impl.ioc_helpers');
goog.require('cljs.core.async.impl.protocols');
goog.require('cljs.core.async.impl.buffers');
goog.require('cljs.core.async.impl.timers');
cljs.core.async.fn_handler = (function cljs$core$async$fn_handler(f){
if(typeof cljs.core.async.t38518 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t38518 = (function (fn_handler,f,meta38519){
this.fn_handler = fn_handler;
this.f = f;
this.meta38519 = meta38519;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t38518.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_38520,meta38519__$1){
var self__ = this;
var _38520__$1 = this;
return (new cljs.core.async.t38518(self__.fn_handler,self__.f,meta38519__$1));
});

cljs.core.async.t38518.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_38520){
var self__ = this;
var _38520__$1 = this;
return self__.meta38519;
});

cljs.core.async.t38518.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t38518.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return true;
});

cljs.core.async.t38518.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.f;
});

cljs.core.async.t38518.getBasis = (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"fn-handler","fn-handler",648785851,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"meta38519","meta38519",97249192,null)], null);
});

cljs.core.async.t38518.cljs$lang$type = true;

cljs.core.async.t38518.cljs$lang$ctorStr = "cljs.core.async/t38518";

cljs.core.async.t38518.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t38518");
});

cljs.core.async.__GT_t38518 = (function cljs$core$async$fn_handler_$___GT_t38518(fn_handler__$1,f__$1,meta38519){
return (new cljs.core.async.t38518(fn_handler__$1,f__$1,meta38519));
});

}

return (new cljs.core.async.t38518(cljs$core$async$fn_handler,f,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Returns a fixed buffer of size n. When full, puts will block/park.
 */
cljs.core.async.buffer = (function cljs$core$async$buffer(n){
return cljs.core.async.impl.buffers.fixed_buffer.call(null,n);
});
/**
 * Returns a buffer of size n. When full, puts will complete but
 * val will be dropped (no transfer).
 */
cljs.core.async.dropping_buffer = (function cljs$core$async$dropping_buffer(n){
return cljs.core.async.impl.buffers.dropping_buffer.call(null,n);
});
/**
 * Returns a buffer of size n. When full, puts will complete, and be
 * buffered, but oldest elements in buffer will be dropped (not
 * transferred).
 */
cljs.core.async.sliding_buffer = (function cljs$core$async$sliding_buffer(n){
return cljs.core.async.impl.buffers.sliding_buffer.call(null,n);
});
/**
 * Returns true if a channel created with buff will never block. That is to say,
 * puts into this buffer will never cause the buffer to be full.
 */
cljs.core.async.unblocking_buffer_QMARK_ = (function cljs$core$async$unblocking_buffer_QMARK_(buff){
var G__38522 = buff;
if(G__38522){
var bit__16743__auto__ = null;
if(cljs.core.truth_((function (){var or__16069__auto__ = bit__16743__auto__;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return G__38522.cljs$core$async$impl$protocols$UnblockingBuffer$;
}
})())){
return true;
} else {
if((!G__38522.cljs$lang$protocol_mask$partition$)){
return cljs.core.native_satisfies_QMARK_.call(null,cljs.core.async.impl.protocols.UnblockingBuffer,G__38522);
} else {
return false;
}
}
} else {
return cljs.core.native_satisfies_QMARK_.call(null,cljs.core.async.impl.protocols.UnblockingBuffer,G__38522);
}
});
/**
 * Creates a channel with an optional buffer, an optional transducer (like (map f),
 * (filter p) etc or a composition thereof), and an optional exception handler.
 * If buf-or-n is a number, will create and use a fixed buffer of that size. If a
 * transducer is supplied a buffer must be specified. ex-handler must be a
 * fn of one argument - if an exception occurs during transformation it will be called
 * with the thrown value as an argument, and any non-nil return value will be placed
 * in the channel.
 */
cljs.core.async.chan = (function cljs$core$async$chan(){
var G__38524 = arguments.length;
switch (G__38524) {
case 0:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.chan.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$0 = (function (){
return cljs.core.async.chan.call(null,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$1 = (function (buf_or_n){
return cljs.core.async.chan.call(null,buf_or_n,null,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$2 = (function (buf_or_n,xform){
return cljs.core.async.chan.call(null,buf_or_n,xform,null);
});

cljs.core.async.chan.cljs$core$IFn$_invoke$arity$3 = (function (buf_or_n,xform,ex_handler){
var buf_or_n__$1 = ((cljs.core._EQ_.call(null,buf_or_n,(0)))?null:buf_or_n);
if(cljs.core.truth_(xform)){
if(cljs.core.truth_(buf_or_n__$1)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str("buffer must be supplied when transducer is"),cljs.core.str("\n"),cljs.core.str(cljs.core.pr_str.call(null,new cljs.core.Symbol(null,"buf-or-n","buf-or-n",-1646815050,null)))].join('')));
}
} else {
}

return cljs.core.async.impl.channels.chan.call(null,((typeof buf_or_n__$1 === 'number')?cljs.core.async.buffer.call(null,buf_or_n__$1):buf_or_n__$1),xform,ex_handler);
});

cljs.core.async.chan.cljs$lang$maxFixedArity = 3;
/**
 * Returns a channel that will close after msecs
 */
cljs.core.async.timeout = (function cljs$core$async$timeout(msecs){
return cljs.core.async.impl.timers.timeout.call(null,msecs);
});
/**
 * takes a val from port. Must be called inside a (go ...) block. Will
 * return nil if closed. Will park if nothing is available.
 * Returns true unless port is already closed
 */
cljs.core.async._LT__BANG_ = (function cljs$core$async$_LT__BANG_(port){
throw (new Error("<! used not in (go ...) block"));
});
/**
 * Asynchronously takes a val from port, passing to fn1. Will pass nil
 * if closed. If on-caller? (default true) is true, and value is
 * immediately available, will call fn1 on calling thread.
 * Returns nil.
 */
cljs.core.async.take_BANG_ = (function cljs$core$async$take_BANG_(){
var G__38527 = arguments.length;
switch (G__38527) {
case 2:
return cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$2 = (function (port,fn1){
return cljs.core.async.take_BANG_.call(null,port,fn1,true);
});

cljs.core.async.take_BANG_.cljs$core$IFn$_invoke$arity$3 = (function (port,fn1,on_caller_QMARK_){
var ret = cljs.core.async.impl.protocols.take_BANG_.call(null,port,cljs.core.async.fn_handler.call(null,fn1));
if(cljs.core.truth_(ret)){
var val_38529 = cljs.core.deref.call(null,ret);
if(cljs.core.truth_(on_caller_QMARK_)){
fn1.call(null,val_38529);
} else {
cljs.core.async.impl.dispatch.run.call(null,((function (val_38529,ret){
return (function (){
return fn1.call(null,val_38529);
});})(val_38529,ret))
);
}
} else {
}

return null;
});

cljs.core.async.take_BANG_.cljs$lang$maxFixedArity = 3;
cljs.core.async.nop = (function cljs$core$async$nop(_){
return null;
});
cljs.core.async.fhnop = cljs.core.async.fn_handler.call(null,cljs.core.async.nop);
/**
 * puts a val into port. nil values are not allowed. Must be called
 * inside a (go ...) block. Will park if no buffer space is available.
 * Returns true unless port is already closed.
 */
cljs.core.async._GT__BANG_ = (function cljs$core$async$_GT__BANG_(port,val){
throw (new Error(">! used not in (go ...) block"));
});
/**
 * Asynchronously puts a val into port, calling fn0 (if supplied) when
 * complete. nil values are not allowed. Will throw if closed. If
 * on-caller? (default true) is true, and the put is immediately
 * accepted, will call fn0 on calling thread.  Returns nil.
 */
cljs.core.async.put_BANG_ = (function cljs$core$async$put_BANG_(){
var G__38531 = arguments.length;
switch (G__38531) {
case 2:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$2 = (function (port,val){
var temp__4423__auto__ = cljs.core.async.impl.protocols.put_BANG_.call(null,port,val,cljs.core.async.fhnop);
if(cljs.core.truth_(temp__4423__auto__)){
var ret = temp__4423__auto__;
return cljs.core.deref.call(null,ret);
} else {
return true;
}
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$3 = (function (port,val,fn1){
return cljs.core.async.put_BANG_.call(null,port,val,fn1,true);
});

cljs.core.async.put_BANG_.cljs$core$IFn$_invoke$arity$4 = (function (port,val,fn1,on_caller_QMARK_){
var temp__4423__auto__ = cljs.core.async.impl.protocols.put_BANG_.call(null,port,val,cljs.core.async.fn_handler.call(null,fn1));
if(cljs.core.truth_(temp__4423__auto__)){
var retb = temp__4423__auto__;
var ret = cljs.core.deref.call(null,retb);
if(cljs.core.truth_(on_caller_QMARK_)){
fn1.call(null,ret);
} else {
cljs.core.async.impl.dispatch.run.call(null,((function (ret,retb,temp__4423__auto__){
return (function (){
return fn1.call(null,ret);
});})(ret,retb,temp__4423__auto__))
);
}

return ret;
} else {
return true;
}
});

cljs.core.async.put_BANG_.cljs$lang$maxFixedArity = 4;
cljs.core.async.close_BANG_ = (function cljs$core$async$close_BANG_(port){
return cljs.core.async.impl.protocols.close_BANG_.call(null,port);
});
cljs.core.async.random_array = (function cljs$core$async$random_array(n){
var a = (new Array(n));
var n__16954__auto___38533 = n;
var x_38534 = (0);
while(true){
if((x_38534 < n__16954__auto___38533)){
(a[x_38534] = (0));

var G__38535 = (x_38534 + (1));
x_38534 = G__38535;
continue;
} else {
}
break;
}

var i = (1);
while(true){
if(cljs.core._EQ_.call(null,i,n)){
return a;
} else {
var j = cljs.core.rand_int.call(null,i);
(a[i] = (a[j]));

(a[j] = i);

var G__38536 = (i + (1));
i = G__38536;
continue;
}
break;
}
});
cljs.core.async.alt_flag = (function cljs$core$async$alt_flag(){
var flag = cljs.core.atom.call(null,true);
if(typeof cljs.core.async.t38540 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t38540 = (function (alt_flag,flag,meta38541){
this.alt_flag = alt_flag;
this.flag = flag;
this.meta38541 = meta38541;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t38540.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (flag){
return (function (_38542,meta38541__$1){
var self__ = this;
var _38542__$1 = this;
return (new cljs.core.async.t38540(self__.alt_flag,self__.flag,meta38541__$1));
});})(flag))
;

cljs.core.async.t38540.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (flag){
return (function (_38542){
var self__ = this;
var _38542__$1 = this;
return self__.meta38541;
});})(flag))
;

cljs.core.async.t38540.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t38540.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = ((function (flag){
return (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.deref.call(null,self__.flag);
});})(flag))
;

cljs.core.async.t38540.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = ((function (flag){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.flag,null);

return true;
});})(flag))
;

cljs.core.async.t38540.getBasis = ((function (flag){
return (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"alt-flag","alt-flag",-1794972754,null),new cljs.core.Symbol(null,"flag","flag",-1565787888,null),new cljs.core.Symbol(null,"meta38541","meta38541",-329373567,null)], null);
});})(flag))
;

cljs.core.async.t38540.cljs$lang$type = true;

cljs.core.async.t38540.cljs$lang$ctorStr = "cljs.core.async/t38540";

cljs.core.async.t38540.cljs$lang$ctorPrWriter = ((function (flag){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t38540");
});})(flag))
;

cljs.core.async.__GT_t38540 = ((function (flag){
return (function cljs$core$async$alt_flag_$___GT_t38540(alt_flag__$1,flag__$1,meta38541){
return (new cljs.core.async.t38540(alt_flag__$1,flag__$1,meta38541));
});})(flag))
;

}

return (new cljs.core.async.t38540(cljs$core$async$alt_flag,flag,cljs.core.PersistentArrayMap.EMPTY));
});
cljs.core.async.alt_handler = (function cljs$core$async$alt_handler(flag,cb){
if(typeof cljs.core.async.t38546 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t38546 = (function (alt_handler,flag,cb,meta38547){
this.alt_handler = alt_handler;
this.flag = flag;
this.cb = cb;
this.meta38547 = meta38547;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t38546.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_38548,meta38547__$1){
var self__ = this;
var _38548__$1 = this;
return (new cljs.core.async.t38546(self__.alt_handler,self__.flag,self__.cb,meta38547__$1));
});

cljs.core.async.t38546.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_38548){
var self__ = this;
var _38548__$1 = this;
return self__.meta38547;
});

cljs.core.async.t38546.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t38546.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.active_QMARK_.call(null,self__.flag);
});

cljs.core.async.t38546.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.async.impl.protocols.commit.call(null,self__.flag);

return self__.cb;
});

cljs.core.async.t38546.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"alt-handler","alt-handler",963786170,null),new cljs.core.Symbol(null,"flag","flag",-1565787888,null),new cljs.core.Symbol(null,"cb","cb",-2064487928,null),new cljs.core.Symbol(null,"meta38547","meta38547",385916929,null)], null);
});

cljs.core.async.t38546.cljs$lang$type = true;

cljs.core.async.t38546.cljs$lang$ctorStr = "cljs.core.async/t38546";

cljs.core.async.t38546.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t38546");
});

cljs.core.async.__GT_t38546 = (function cljs$core$async$alt_handler_$___GT_t38546(alt_handler__$1,flag__$1,cb__$1,meta38547){
return (new cljs.core.async.t38546(alt_handler__$1,flag__$1,cb__$1,meta38547));
});

}

return (new cljs.core.async.t38546(cljs$core$async$alt_handler,flag,cb,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * returns derefable [val port] if immediate, nil if enqueued
 */
cljs.core.async.do_alts = (function cljs$core$async$do_alts(fret,ports,opts){
var flag = cljs.core.async.alt_flag.call(null);
var n = cljs.core.count.call(null,ports);
var idxs = cljs.core.async.random_array.call(null,n);
var priority = new cljs.core.Keyword(null,"priority","priority",1431093715).cljs$core$IFn$_invoke$arity$1(opts);
var ret = (function (){var i = (0);
while(true){
if((i < n)){
var idx = (cljs.core.truth_(priority)?i:(idxs[i]));
var port = cljs.core.nth.call(null,ports,idx);
var wport = ((cljs.core.vector_QMARK_.call(null,port))?port.call(null,(0)):null);
var vbox = (cljs.core.truth_(wport)?(function (){var val = port.call(null,(1));
return cljs.core.async.impl.protocols.put_BANG_.call(null,wport,val,cljs.core.async.alt_handler.call(null,flag,((function (i,val,idx,port,wport,flag,n,idxs,priority){
return (function (p1__38549_SHARP_){
return fret.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [p1__38549_SHARP_,wport], null));
});})(i,val,idx,port,wport,flag,n,idxs,priority))
));
})():cljs.core.async.impl.protocols.take_BANG_.call(null,port,cljs.core.async.alt_handler.call(null,flag,((function (i,idx,port,wport,flag,n,idxs,priority){
return (function (p1__38550_SHARP_){
return fret.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [p1__38550_SHARP_,port], null));
});})(i,idx,port,wport,flag,n,idxs,priority))
)));
if(cljs.core.truth_(vbox)){
return cljs.core.async.impl.channels.box.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [cljs.core.deref.call(null,vbox),(function (){var or__16069__auto__ = wport;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return port;
}
})()], null));
} else {
var G__38551 = (i + (1));
i = G__38551;
continue;
}
} else {
return null;
}
break;
}
})();
var or__16069__auto__ = ret;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
if(cljs.core.contains_QMARK_.call(null,opts,new cljs.core.Keyword(null,"default","default",-1987822328))){
var temp__4425__auto__ = (function (){var and__16057__auto__ = cljs.core.async.impl.protocols.active_QMARK_.call(null,flag);
if(cljs.core.truth_(and__16057__auto__)){
return cljs.core.async.impl.protocols.commit.call(null,flag);
} else {
return and__16057__auto__;
}
})();
if(cljs.core.truth_(temp__4425__auto__)){
var got = temp__4425__auto__;
return cljs.core.async.impl.channels.box.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"default","default",-1987822328).cljs$core$IFn$_invoke$arity$1(opts),new cljs.core.Keyword(null,"default","default",-1987822328)], null));
} else {
return null;
}
} else {
return null;
}
}
});
/**
 * Completes at most one of several channel operations. Must be called
 * inside a (go ...) block. ports is a vector of channel endpoints,
 * which can be either a channel to take from or a vector of
 * [channel-to-put-to val-to-put], in any combination. Takes will be
 * made as if by <!, and puts will be made as if by >!. Unless
 * the :priority option is true, if more than one port operation is
 * ready a non-deterministic choice will be made. If no operation is
 * ready and a :default value is supplied, [default-val :default] will
 * be returned, otherwise alts! will park until the first operation to
 * become ready completes. Returns [val port] of the completed
 * operation, where val is the value taken for takes, and a
 * boolean (true unless already closed, as per put!) for puts.
 * 
 * opts are passed as :key val ... Supported options:
 * 
 * :default val - the value to use if none of the operations are immediately ready
 * :priority true - (default nil) when true, the operations will be tried in order.
 * 
 * Note: there is no guarantee that the port exps or val exprs will be
 * used, nor in what order should they be, so they should not be
 * depended upon for side effects.
 */
cljs.core.async.alts_BANG_ = (function cljs$core$async$alts_BANG_(){
var argseq__17109__auto__ = ((((1) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(1)),(0))):null);
return cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),argseq__17109__auto__);
});

cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic = (function (ports,p__38554){
var map__38555 = p__38554;
var map__38555__$1 = ((cljs.core.seq_QMARK_.call(null,map__38555))?cljs.core.apply.call(null,cljs.core.hash_map,map__38555):map__38555);
var opts = map__38555__$1;
throw (new Error("alts! used not in (go ...) block"));
});

cljs.core.async.alts_BANG_.cljs$lang$maxFixedArity = (1);

cljs.core.async.alts_BANG_.cljs$lang$applyTo = (function (seq38552){
var G__38553 = cljs.core.first.call(null,seq38552);
var seq38552__$1 = cljs.core.next.call(null,seq38552);
return cljs.core.async.alts_BANG_.cljs$core$IFn$_invoke$arity$variadic(G__38553,seq38552__$1);
});
/**
 * Takes elements from the from channel and supplies them to the to
 * channel. By default, the to channel will be closed when the from
 * channel closes, but can be determined by the close?  parameter. Will
 * stop consuming the from channel if the to channel closes
 */
cljs.core.async.pipe = (function cljs$core$async$pipe(){
var G__38557 = arguments.length;
switch (G__38557) {
case 2:
return cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$2 = (function (from,to){
return cljs.core.async.pipe.call(null,from,to,true);
});

cljs.core.async.pipe.cljs$core$IFn$_invoke$arity$3 = (function (from,to,close_QMARK_){
var c__23633__auto___38606 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___38606){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___38606){
return (function (state_38581){
var state_val_38582 = (state_38581[(1)]);
if((state_val_38582 === (7))){
var inst_38577 = (state_38581[(2)]);
var state_38581__$1 = state_38581;
var statearr_38583_38607 = state_38581__$1;
(statearr_38583_38607[(2)] = inst_38577);

(statearr_38583_38607[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (1))){
var state_38581__$1 = state_38581;
var statearr_38584_38608 = state_38581__$1;
(statearr_38584_38608[(2)] = null);

(statearr_38584_38608[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (4))){
var inst_38560 = (state_38581[(7)]);
var inst_38560__$1 = (state_38581[(2)]);
var inst_38561 = (inst_38560__$1 == null);
var state_38581__$1 = (function (){var statearr_38585 = state_38581;
(statearr_38585[(7)] = inst_38560__$1);

return statearr_38585;
})();
if(cljs.core.truth_(inst_38561)){
var statearr_38586_38609 = state_38581__$1;
(statearr_38586_38609[(1)] = (5));

} else {
var statearr_38587_38610 = state_38581__$1;
(statearr_38587_38610[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (13))){
var state_38581__$1 = state_38581;
var statearr_38588_38611 = state_38581__$1;
(statearr_38588_38611[(2)] = null);

(statearr_38588_38611[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (6))){
var inst_38560 = (state_38581[(7)]);
var state_38581__$1 = state_38581;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_38581__$1,(11),to,inst_38560);
} else {
if((state_val_38582 === (3))){
var inst_38579 = (state_38581[(2)]);
var state_38581__$1 = state_38581;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38581__$1,inst_38579);
} else {
if((state_val_38582 === (12))){
var state_38581__$1 = state_38581;
var statearr_38589_38612 = state_38581__$1;
(statearr_38589_38612[(2)] = null);

(statearr_38589_38612[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (2))){
var state_38581__$1 = state_38581;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38581__$1,(4),from);
} else {
if((state_val_38582 === (11))){
var inst_38570 = (state_38581[(2)]);
var state_38581__$1 = state_38581;
if(cljs.core.truth_(inst_38570)){
var statearr_38590_38613 = state_38581__$1;
(statearr_38590_38613[(1)] = (12));

} else {
var statearr_38591_38614 = state_38581__$1;
(statearr_38591_38614[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (9))){
var state_38581__$1 = state_38581;
var statearr_38592_38615 = state_38581__$1;
(statearr_38592_38615[(2)] = null);

(statearr_38592_38615[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (5))){
var state_38581__$1 = state_38581;
if(cljs.core.truth_(close_QMARK_)){
var statearr_38593_38616 = state_38581__$1;
(statearr_38593_38616[(1)] = (8));

} else {
var statearr_38594_38617 = state_38581__$1;
(statearr_38594_38617[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (14))){
var inst_38575 = (state_38581[(2)]);
var state_38581__$1 = state_38581;
var statearr_38595_38618 = state_38581__$1;
(statearr_38595_38618[(2)] = inst_38575);

(statearr_38595_38618[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (10))){
var inst_38567 = (state_38581[(2)]);
var state_38581__$1 = state_38581;
var statearr_38596_38619 = state_38581__$1;
(statearr_38596_38619[(2)] = inst_38567);

(statearr_38596_38619[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38582 === (8))){
var inst_38564 = cljs.core.async.close_BANG_.call(null,to);
var state_38581__$1 = state_38581;
var statearr_38597_38620 = state_38581__$1;
(statearr_38597_38620[(2)] = inst_38564);

(statearr_38597_38620[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___38606))
;
return ((function (switch__23571__auto__,c__23633__auto___38606){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_38601 = [null,null,null,null,null,null,null,null];
(statearr_38601[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_38601[(1)] = (1));

return statearr_38601;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_38581){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38581);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38602){if((e38602 instanceof Object)){
var ex__23575__auto__ = e38602;
var statearr_38603_38621 = state_38581;
(statearr_38603_38621[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38581);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38602;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__38622 = state_38581;
state_38581 = G__38622;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_38581){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_38581);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___38606))
})();
var state__23635__auto__ = (function (){var statearr_38604 = f__23634__auto__.call(null);
(statearr_38604[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___38606);

return statearr_38604;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___38606))
);


return to;
});

cljs.core.async.pipe.cljs$lang$maxFixedArity = 3;
cljs.core.async.pipeline_STAR_ = (function cljs$core$async$pipeline_STAR_(n,to,xf,from,close_QMARK_,ex_handler,type){
if((n > (0))){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol(null,"pos?","pos?",-244377722,null),new cljs.core.Symbol(null,"n","n",-2092305744,null))))].join('')));
}

var jobs = cljs.core.async.chan.call(null,n);
var results = cljs.core.async.chan.call(null,n);
var process = ((function (jobs,results){
return (function (p__38806){
var vec__38807 = p__38806;
var v = cljs.core.nth.call(null,vec__38807,(0),null);
var p = cljs.core.nth.call(null,vec__38807,(1),null);
var job = vec__38807;
if((job == null)){
cljs.core.async.close_BANG_.call(null,results);

return null;
} else {
var res = cljs.core.async.chan.call(null,(1),xf,ex_handler);
var c__23633__auto___38989 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results){
return (function (state_38812){
var state_val_38813 = (state_38812[(1)]);
if((state_val_38813 === (1))){
var state_38812__$1 = state_38812;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_38812__$1,(2),res,v);
} else {
if((state_val_38813 === (2))){
var inst_38809 = (state_38812[(2)]);
var inst_38810 = cljs.core.async.close_BANG_.call(null,res);
var state_38812__$1 = (function (){var statearr_38814 = state_38812;
(statearr_38814[(7)] = inst_38809);

return statearr_38814;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38812__$1,inst_38810);
} else {
return null;
}
}
});})(c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results))
;
return ((function (switch__23571__auto__,c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_38818 = [null,null,null,null,null,null,null,null];
(statearr_38818[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__);

(statearr_38818[(1)] = (1));

return statearr_38818;
});
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1 = (function (state_38812){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38812);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38819){if((e38819 instanceof Object)){
var ex__23575__auto__ = e38819;
var statearr_38820_38990 = state_38812;
(statearr_38820_38990[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38812);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38819;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__38991 = state_38812;
state_38812 = G__38991;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = function(state_38812){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1.call(this,state_38812);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results))
})();
var state__23635__auto__ = (function (){var statearr_38821 = f__23634__auto__.call(null);
(statearr_38821[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___38989);

return statearr_38821;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___38989,res,vec__38807,v,p,job,jobs,results))
);


cljs.core.async.put_BANG_.call(null,p,res);

return true;
}
});})(jobs,results))
;
var async = ((function (jobs,results,process){
return (function (p__38822){
var vec__38823 = p__38822;
var v = cljs.core.nth.call(null,vec__38823,(0),null);
var p = cljs.core.nth.call(null,vec__38823,(1),null);
var job = vec__38823;
if((job == null)){
cljs.core.async.close_BANG_.call(null,results);

return null;
} else {
var res = cljs.core.async.chan.call(null,(1));
xf.call(null,v,res);

cljs.core.async.put_BANG_.call(null,p,res);

return true;
}
});})(jobs,results,process))
;
var n__16954__auto___38992 = n;
var __38993 = (0);
while(true){
if((__38993 < n__16954__auto___38992)){
var G__38824_38994 = (((type instanceof cljs.core.Keyword))?type.fqn:null);
switch (G__38824_38994) {
case "compute":
var c__23633__auto___38996 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (__38993,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (__38993,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function (state_38837){
var state_val_38838 = (state_38837[(1)]);
if((state_val_38838 === (1))){
var state_38837__$1 = state_38837;
var statearr_38839_38997 = state_38837__$1;
(statearr_38839_38997[(2)] = null);

(statearr_38839_38997[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38838 === (2))){
var state_38837__$1 = state_38837;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38837__$1,(4),jobs);
} else {
if((state_val_38838 === (3))){
var inst_38835 = (state_38837[(2)]);
var state_38837__$1 = state_38837;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38837__$1,inst_38835);
} else {
if((state_val_38838 === (4))){
var inst_38827 = (state_38837[(2)]);
var inst_38828 = process.call(null,inst_38827);
var state_38837__$1 = state_38837;
if(cljs.core.truth_(inst_38828)){
var statearr_38840_38998 = state_38837__$1;
(statearr_38840_38998[(1)] = (5));

} else {
var statearr_38841_38999 = state_38837__$1;
(statearr_38841_38999[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38838 === (5))){
var state_38837__$1 = state_38837;
var statearr_38842_39000 = state_38837__$1;
(statearr_38842_39000[(2)] = null);

(statearr_38842_39000[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38838 === (6))){
var state_38837__$1 = state_38837;
var statearr_38843_39001 = state_38837__$1;
(statearr_38843_39001[(2)] = null);

(statearr_38843_39001[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38838 === (7))){
var inst_38833 = (state_38837[(2)]);
var state_38837__$1 = state_38837;
var statearr_38844_39002 = state_38837__$1;
(statearr_38844_39002[(2)] = inst_38833);

(statearr_38844_39002[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
});})(__38993,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
;
return ((function (__38993,switch__23571__auto__,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_38848 = [null,null,null,null,null,null,null];
(statearr_38848[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__);

(statearr_38848[(1)] = (1));

return statearr_38848;
});
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1 = (function (state_38837){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38837);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38849){if((e38849 instanceof Object)){
var ex__23575__auto__ = e38849;
var statearr_38850_39003 = state_38837;
(statearr_38850_39003[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38837);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38849;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39004 = state_38837;
state_38837 = G__39004;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = function(state_38837){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1.call(this,state_38837);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__;
})()
;})(__38993,switch__23571__auto__,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
})();
var state__23635__auto__ = (function (){var statearr_38851 = f__23634__auto__.call(null);
(statearr_38851[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___38996);

return statearr_38851;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(__38993,c__23633__auto___38996,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
);


break;
case "async":
var c__23633__auto___39005 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (__38993,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (__38993,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function (state_38864){
var state_val_38865 = (state_38864[(1)]);
if((state_val_38865 === (1))){
var state_38864__$1 = state_38864;
var statearr_38866_39006 = state_38864__$1;
(statearr_38866_39006[(2)] = null);

(statearr_38866_39006[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38865 === (2))){
var state_38864__$1 = state_38864;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38864__$1,(4),jobs);
} else {
if((state_val_38865 === (3))){
var inst_38862 = (state_38864[(2)]);
var state_38864__$1 = state_38864;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38864__$1,inst_38862);
} else {
if((state_val_38865 === (4))){
var inst_38854 = (state_38864[(2)]);
var inst_38855 = async.call(null,inst_38854);
var state_38864__$1 = state_38864;
if(cljs.core.truth_(inst_38855)){
var statearr_38867_39007 = state_38864__$1;
(statearr_38867_39007[(1)] = (5));

} else {
var statearr_38868_39008 = state_38864__$1;
(statearr_38868_39008[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38865 === (5))){
var state_38864__$1 = state_38864;
var statearr_38869_39009 = state_38864__$1;
(statearr_38869_39009[(2)] = null);

(statearr_38869_39009[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38865 === (6))){
var state_38864__$1 = state_38864;
var statearr_38870_39010 = state_38864__$1;
(statearr_38870_39010[(2)] = null);

(statearr_38870_39010[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38865 === (7))){
var inst_38860 = (state_38864[(2)]);
var state_38864__$1 = state_38864;
var statearr_38871_39011 = state_38864__$1;
(statearr_38871_39011[(2)] = inst_38860);

(statearr_38871_39011[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
});})(__38993,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
;
return ((function (__38993,switch__23571__auto__,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_38875 = [null,null,null,null,null,null,null];
(statearr_38875[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__);

(statearr_38875[(1)] = (1));

return statearr_38875;
});
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1 = (function (state_38864){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38864);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38876){if((e38876 instanceof Object)){
var ex__23575__auto__ = e38876;
var statearr_38877_39012 = state_38864;
(statearr_38877_39012[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38864);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38876;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39013 = state_38864;
state_38864 = G__39013;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = function(state_38864){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1.call(this,state_38864);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__;
})()
;})(__38993,switch__23571__auto__,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
})();
var state__23635__auto__ = (function (){var statearr_38878 = f__23634__auto__.call(null);
(statearr_38878[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___39005);

return statearr_38878;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(__38993,c__23633__auto___39005,G__38824_38994,n__16954__auto___38992,jobs,results,process,async))
);


break;
default:
throw (new Error([cljs.core.str("No matching clause: "),cljs.core.str(type)].join('')));

}

var G__39014 = (__38993 + (1));
__38993 = G__39014;
continue;
} else {
}
break;
}

var c__23633__auto___39015 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___39015,jobs,results,process,async){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___39015,jobs,results,process,async){
return (function (state_38900){
var state_val_38901 = (state_38900[(1)]);
if((state_val_38901 === (1))){
var state_38900__$1 = state_38900;
var statearr_38902_39016 = state_38900__$1;
(statearr_38902_39016[(2)] = null);

(statearr_38902_39016[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38901 === (2))){
var state_38900__$1 = state_38900;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38900__$1,(4),from);
} else {
if((state_val_38901 === (3))){
var inst_38898 = (state_38900[(2)]);
var state_38900__$1 = state_38900;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38900__$1,inst_38898);
} else {
if((state_val_38901 === (4))){
var inst_38881 = (state_38900[(7)]);
var inst_38881__$1 = (state_38900[(2)]);
var inst_38882 = (inst_38881__$1 == null);
var state_38900__$1 = (function (){var statearr_38903 = state_38900;
(statearr_38903[(7)] = inst_38881__$1);

return statearr_38903;
})();
if(cljs.core.truth_(inst_38882)){
var statearr_38904_39017 = state_38900__$1;
(statearr_38904_39017[(1)] = (5));

} else {
var statearr_38905_39018 = state_38900__$1;
(statearr_38905_39018[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38901 === (5))){
var inst_38884 = cljs.core.async.close_BANG_.call(null,jobs);
var state_38900__$1 = state_38900;
var statearr_38906_39019 = state_38900__$1;
(statearr_38906_39019[(2)] = inst_38884);

(statearr_38906_39019[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38901 === (6))){
var inst_38881 = (state_38900[(7)]);
var inst_38886 = (state_38900[(8)]);
var inst_38886__$1 = cljs.core.async.chan.call(null,(1));
var inst_38887 = cljs.core.PersistentVector.EMPTY_NODE;
var inst_38888 = [inst_38881,inst_38886__$1];
var inst_38889 = (new cljs.core.PersistentVector(null,2,(5),inst_38887,inst_38888,null));
var state_38900__$1 = (function (){var statearr_38907 = state_38900;
(statearr_38907[(8)] = inst_38886__$1);

return statearr_38907;
})();
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_38900__$1,(8),jobs,inst_38889);
} else {
if((state_val_38901 === (7))){
var inst_38896 = (state_38900[(2)]);
var state_38900__$1 = state_38900;
var statearr_38908_39020 = state_38900__$1;
(statearr_38908_39020[(2)] = inst_38896);

(statearr_38908_39020[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38901 === (8))){
var inst_38886 = (state_38900[(8)]);
var inst_38891 = (state_38900[(2)]);
var state_38900__$1 = (function (){var statearr_38909 = state_38900;
(statearr_38909[(9)] = inst_38891);

return statearr_38909;
})();
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_38900__$1,(9),results,inst_38886);
} else {
if((state_val_38901 === (9))){
var inst_38893 = (state_38900[(2)]);
var state_38900__$1 = (function (){var statearr_38910 = state_38900;
(statearr_38910[(10)] = inst_38893);

return statearr_38910;
})();
var statearr_38911_39021 = state_38900__$1;
(statearr_38911_39021[(2)] = null);

(statearr_38911_39021[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___39015,jobs,results,process,async))
;
return ((function (switch__23571__auto__,c__23633__auto___39015,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_38915 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_38915[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__);

(statearr_38915[(1)] = (1));

return statearr_38915;
});
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1 = (function (state_38900){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38900);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38916){if((e38916 instanceof Object)){
var ex__23575__auto__ = e38916;
var statearr_38917_39022 = state_38900;
(statearr_38917_39022[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38900);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38916;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39023 = state_38900;
state_38900 = G__39023;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = function(state_38900){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1.call(this,state_38900);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___39015,jobs,results,process,async))
})();
var state__23635__auto__ = (function (){var statearr_38918 = f__23634__auto__.call(null);
(statearr_38918[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___39015);

return statearr_38918;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___39015,jobs,results,process,async))
);


var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__,jobs,results,process,async){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__,jobs,results,process,async){
return (function (state_38956){
var state_val_38957 = (state_38956[(1)]);
if((state_val_38957 === (7))){
var inst_38952 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
var statearr_38958_39024 = state_38956__$1;
(statearr_38958_39024[(2)] = inst_38952);

(statearr_38958_39024[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (20))){
var state_38956__$1 = state_38956;
var statearr_38959_39025 = state_38956__$1;
(statearr_38959_39025[(2)] = null);

(statearr_38959_39025[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (1))){
var state_38956__$1 = state_38956;
var statearr_38960_39026 = state_38956__$1;
(statearr_38960_39026[(2)] = null);

(statearr_38960_39026[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (4))){
var inst_38921 = (state_38956[(7)]);
var inst_38921__$1 = (state_38956[(2)]);
var inst_38922 = (inst_38921__$1 == null);
var state_38956__$1 = (function (){var statearr_38961 = state_38956;
(statearr_38961[(7)] = inst_38921__$1);

return statearr_38961;
})();
if(cljs.core.truth_(inst_38922)){
var statearr_38962_39027 = state_38956__$1;
(statearr_38962_39027[(1)] = (5));

} else {
var statearr_38963_39028 = state_38956__$1;
(statearr_38963_39028[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (15))){
var inst_38934 = (state_38956[(8)]);
var state_38956__$1 = state_38956;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_38956__$1,(18),to,inst_38934);
} else {
if((state_val_38957 === (21))){
var inst_38947 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
var statearr_38964_39029 = state_38956__$1;
(statearr_38964_39029[(2)] = inst_38947);

(statearr_38964_39029[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (13))){
var inst_38949 = (state_38956[(2)]);
var state_38956__$1 = (function (){var statearr_38965 = state_38956;
(statearr_38965[(9)] = inst_38949);

return statearr_38965;
})();
var statearr_38966_39030 = state_38956__$1;
(statearr_38966_39030[(2)] = null);

(statearr_38966_39030[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (6))){
var inst_38921 = (state_38956[(7)]);
var state_38956__$1 = state_38956;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38956__$1,(11),inst_38921);
} else {
if((state_val_38957 === (17))){
var inst_38942 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
if(cljs.core.truth_(inst_38942)){
var statearr_38967_39031 = state_38956__$1;
(statearr_38967_39031[(1)] = (19));

} else {
var statearr_38968_39032 = state_38956__$1;
(statearr_38968_39032[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (3))){
var inst_38954 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_38956__$1,inst_38954);
} else {
if((state_val_38957 === (12))){
var inst_38931 = (state_38956[(10)]);
var state_38956__$1 = state_38956;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38956__$1,(14),inst_38931);
} else {
if((state_val_38957 === (2))){
var state_38956__$1 = state_38956;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_38956__$1,(4),results);
} else {
if((state_val_38957 === (19))){
var state_38956__$1 = state_38956;
var statearr_38969_39033 = state_38956__$1;
(statearr_38969_39033[(2)] = null);

(statearr_38969_39033[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (11))){
var inst_38931 = (state_38956[(2)]);
var state_38956__$1 = (function (){var statearr_38970 = state_38956;
(statearr_38970[(10)] = inst_38931);

return statearr_38970;
})();
var statearr_38971_39034 = state_38956__$1;
(statearr_38971_39034[(2)] = null);

(statearr_38971_39034[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (9))){
var state_38956__$1 = state_38956;
var statearr_38972_39035 = state_38956__$1;
(statearr_38972_39035[(2)] = null);

(statearr_38972_39035[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (5))){
var state_38956__$1 = state_38956;
if(cljs.core.truth_(close_QMARK_)){
var statearr_38973_39036 = state_38956__$1;
(statearr_38973_39036[(1)] = (8));

} else {
var statearr_38974_39037 = state_38956__$1;
(statearr_38974_39037[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (14))){
var inst_38936 = (state_38956[(11)]);
var inst_38934 = (state_38956[(8)]);
var inst_38934__$1 = (state_38956[(2)]);
var inst_38935 = (inst_38934__$1 == null);
var inst_38936__$1 = cljs.core.not.call(null,inst_38935);
var state_38956__$1 = (function (){var statearr_38975 = state_38956;
(statearr_38975[(11)] = inst_38936__$1);

(statearr_38975[(8)] = inst_38934__$1);

return statearr_38975;
})();
if(inst_38936__$1){
var statearr_38976_39038 = state_38956__$1;
(statearr_38976_39038[(1)] = (15));

} else {
var statearr_38977_39039 = state_38956__$1;
(statearr_38977_39039[(1)] = (16));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (16))){
var inst_38936 = (state_38956[(11)]);
var state_38956__$1 = state_38956;
var statearr_38978_39040 = state_38956__$1;
(statearr_38978_39040[(2)] = inst_38936);

(statearr_38978_39040[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (10))){
var inst_38928 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
var statearr_38979_39041 = state_38956__$1;
(statearr_38979_39041[(2)] = inst_38928);

(statearr_38979_39041[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (18))){
var inst_38939 = (state_38956[(2)]);
var state_38956__$1 = state_38956;
var statearr_38980_39042 = state_38956__$1;
(statearr_38980_39042[(2)] = inst_38939);

(statearr_38980_39042[(1)] = (17));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_38957 === (8))){
var inst_38925 = cljs.core.async.close_BANG_.call(null,to);
var state_38956__$1 = state_38956;
var statearr_38981_39043 = state_38956__$1;
(statearr_38981_39043[(2)] = inst_38925);

(statearr_38981_39043[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto__,jobs,results,process,async))
;
return ((function (switch__23571__auto__,c__23633__auto__,jobs,results,process,async){
return (function() {
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_38985 = [null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_38985[(0)] = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__);

(statearr_38985[(1)] = (1));

return statearr_38985;
});
var cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1 = (function (state_38956){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_38956);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e38986){if((e38986 instanceof Object)){
var ex__23575__auto__ = e38986;
var statearr_38987_39044 = state_38956;
(statearr_38987_39044[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_38956);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e38986;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39045 = state_38956;
state_38956 = G__39045;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__ = function(state_38956){
switch(arguments.length){
case 0:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1.call(this,state_38956);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____0;
cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$pipeline_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$pipeline_STAR__$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__,jobs,results,process,async))
})();
var state__23635__auto__ = (function (){var statearr_38988 = f__23634__auto__.call(null);
(statearr_38988[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_38988;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__,jobs,results,process,async))
);

return c__23633__auto__;
});
/**
 * Takes elements from the from channel and supplies them to the to
 * channel, subject to the async function af, with parallelism n. af
 * must be a function of two arguments, the first an input value and
 * the second a channel on which to place the result(s). af must close!
 * the channel before returning.  The presumption is that af will
 * return immediately, having launched some asynchronous operation
 * whose completion/callback will manipulate the result channel. Outputs
 * will be returned in order relative to  the inputs. By default, the to
 * channel will be closed when the from channel closes, but can be
 * determined by the close?  parameter. Will stop consuming the from
 * channel if the to channel closes.
 */
cljs.core.async.pipeline_async = (function cljs$core$async$pipeline_async(){
var G__39047 = arguments.length;
switch (G__39047) {
case 4:
return cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 5:
return cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$5((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$4 = (function (n,to,af,from){
return cljs.core.async.pipeline_async.call(null,n,to,af,from,true);
});

cljs.core.async.pipeline_async.cljs$core$IFn$_invoke$arity$5 = (function (n,to,af,from,close_QMARK_){
return cljs.core.async.pipeline_STAR_.call(null,n,to,af,from,close_QMARK_,null,new cljs.core.Keyword(null,"async","async",1050769601));
});

cljs.core.async.pipeline_async.cljs$lang$maxFixedArity = 5;
/**
 * Takes elements from the from channel and supplies them to the to
 * channel, subject to the transducer xf, with parallelism n. Because
 * it is parallel, the transducer will be applied independently to each
 * element, not across elements, and may produce zero or more outputs
 * per input.  Outputs will be returned in order relative to the
 * inputs. By default, the to channel will be closed when the from
 * channel closes, but can be determined by the close?  parameter. Will
 * stop consuming the from channel if the to channel closes.
 * 
 * Note this is supplied for API compatibility with the Clojure version.
 * Values of N > 1 will not result in actual concurrency in a
 * single-threaded runtime.
 */
cljs.core.async.pipeline = (function cljs$core$async$pipeline(){
var G__39050 = arguments.length;
switch (G__39050) {
case 4:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 5:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$5((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]));

break;
case 6:
return cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$6((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$4 = (function (n,to,xf,from){
return cljs.core.async.pipeline.call(null,n,to,xf,from,true);
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$5 = (function (n,to,xf,from,close_QMARK_){
return cljs.core.async.pipeline.call(null,n,to,xf,from,close_QMARK_,null);
});

cljs.core.async.pipeline.cljs$core$IFn$_invoke$arity$6 = (function (n,to,xf,from,close_QMARK_,ex_handler){
return cljs.core.async.pipeline_STAR_.call(null,n,to,xf,from,close_QMARK_,ex_handler,new cljs.core.Keyword(null,"compute","compute",1555393130));
});

cljs.core.async.pipeline.cljs$lang$maxFixedArity = 6;
/**
 * Takes a predicate and a source channel and returns a vector of two
 * channels, the first of which will contain the values for which the
 * predicate returned true, the second those for which it returned
 * false.
 * 
 * The out channels will be unbuffered by default, or two buf-or-ns can
 * be supplied. The channels will close after the source channel has
 * closed.
 */
cljs.core.async.split = (function cljs$core$async$split(){
var G__39053 = arguments.length;
switch (G__39053) {
case 2:
return cljs.core.async.split.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 4:
return cljs.core.async.split.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.split.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.split.call(null,p,ch,null,null);
});

cljs.core.async.split.cljs$core$IFn$_invoke$arity$4 = (function (p,ch,t_buf_or_n,f_buf_or_n){
var tc = cljs.core.async.chan.call(null,t_buf_or_n);
var fc = cljs.core.async.chan.call(null,f_buf_or_n);
var c__23633__auto___39105 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___39105,tc,fc){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___39105,tc,fc){
return (function (state_39079){
var state_val_39080 = (state_39079[(1)]);
if((state_val_39080 === (7))){
var inst_39075 = (state_39079[(2)]);
var state_39079__$1 = state_39079;
var statearr_39081_39106 = state_39079__$1;
(statearr_39081_39106[(2)] = inst_39075);

(statearr_39081_39106[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (1))){
var state_39079__$1 = state_39079;
var statearr_39082_39107 = state_39079__$1;
(statearr_39082_39107[(2)] = null);

(statearr_39082_39107[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (4))){
var inst_39056 = (state_39079[(7)]);
var inst_39056__$1 = (state_39079[(2)]);
var inst_39057 = (inst_39056__$1 == null);
var state_39079__$1 = (function (){var statearr_39083 = state_39079;
(statearr_39083[(7)] = inst_39056__$1);

return statearr_39083;
})();
if(cljs.core.truth_(inst_39057)){
var statearr_39084_39108 = state_39079__$1;
(statearr_39084_39108[(1)] = (5));

} else {
var statearr_39085_39109 = state_39079__$1;
(statearr_39085_39109[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (13))){
var state_39079__$1 = state_39079;
var statearr_39086_39110 = state_39079__$1;
(statearr_39086_39110[(2)] = null);

(statearr_39086_39110[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (6))){
var inst_39056 = (state_39079[(7)]);
var inst_39062 = p.call(null,inst_39056);
var state_39079__$1 = state_39079;
if(cljs.core.truth_(inst_39062)){
var statearr_39087_39111 = state_39079__$1;
(statearr_39087_39111[(1)] = (9));

} else {
var statearr_39088_39112 = state_39079__$1;
(statearr_39088_39112[(1)] = (10));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (3))){
var inst_39077 = (state_39079[(2)]);
var state_39079__$1 = state_39079;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_39079__$1,inst_39077);
} else {
if((state_val_39080 === (12))){
var state_39079__$1 = state_39079;
var statearr_39089_39113 = state_39079__$1;
(statearr_39089_39113[(2)] = null);

(statearr_39089_39113[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (2))){
var state_39079__$1 = state_39079;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_39079__$1,(4),ch);
} else {
if((state_val_39080 === (11))){
var inst_39056 = (state_39079[(7)]);
var inst_39066 = (state_39079[(2)]);
var state_39079__$1 = state_39079;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_39079__$1,(8),inst_39066,inst_39056);
} else {
if((state_val_39080 === (9))){
var state_39079__$1 = state_39079;
var statearr_39090_39114 = state_39079__$1;
(statearr_39090_39114[(2)] = tc);

(statearr_39090_39114[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (5))){
var inst_39059 = cljs.core.async.close_BANG_.call(null,tc);
var inst_39060 = cljs.core.async.close_BANG_.call(null,fc);
var state_39079__$1 = (function (){var statearr_39091 = state_39079;
(statearr_39091[(8)] = inst_39059);

return statearr_39091;
})();
var statearr_39092_39115 = state_39079__$1;
(statearr_39092_39115[(2)] = inst_39060);

(statearr_39092_39115[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (14))){
var inst_39073 = (state_39079[(2)]);
var state_39079__$1 = state_39079;
var statearr_39093_39116 = state_39079__$1;
(statearr_39093_39116[(2)] = inst_39073);

(statearr_39093_39116[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (10))){
var state_39079__$1 = state_39079;
var statearr_39094_39117 = state_39079__$1;
(statearr_39094_39117[(2)] = fc);

(statearr_39094_39117[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39080 === (8))){
var inst_39068 = (state_39079[(2)]);
var state_39079__$1 = state_39079;
if(cljs.core.truth_(inst_39068)){
var statearr_39095_39118 = state_39079__$1;
(statearr_39095_39118[(1)] = (12));

} else {
var statearr_39096_39119 = state_39079__$1;
(statearr_39096_39119[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___39105,tc,fc))
;
return ((function (switch__23571__auto__,c__23633__auto___39105,tc,fc){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_39100 = [null,null,null,null,null,null,null,null,null];
(statearr_39100[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_39100[(1)] = (1));

return statearr_39100;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_39079){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_39079);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e39101){if((e39101 instanceof Object)){
var ex__23575__auto__ = e39101;
var statearr_39102_39120 = state_39079;
(statearr_39102_39120[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_39079);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e39101;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39121 = state_39079;
state_39079 = G__39121;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_39079){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_39079);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___39105,tc,fc))
})();
var state__23635__auto__ = (function (){var statearr_39103 = f__23634__auto__.call(null);
(statearr_39103[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___39105);

return statearr_39103;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___39105,tc,fc))
);


return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [tc,fc], null);
});

cljs.core.async.split.cljs$lang$maxFixedArity = 4;
/**
 * f should be a function of 2 arguments. Returns a channel containing
 * the single result of applying f to init and the first item from the
 * channel, then applying f to that result and the 2nd item, etc. If
 * the channel closes without yielding items, returns init and f is not
 * called. ch must close before reduce produces a result.
 */
cljs.core.async.reduce = (function cljs$core$async$reduce(f,init,ch){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_39168){
var state_val_39169 = (state_39168[(1)]);
if((state_val_39169 === (1))){
var inst_39154 = init;
var state_39168__$1 = (function (){var statearr_39170 = state_39168;
(statearr_39170[(7)] = inst_39154);

return statearr_39170;
})();
var statearr_39171_39186 = state_39168__$1;
(statearr_39171_39186[(2)] = null);

(statearr_39171_39186[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39169 === (2))){
var state_39168__$1 = state_39168;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_39168__$1,(4),ch);
} else {
if((state_val_39169 === (3))){
var inst_39166 = (state_39168[(2)]);
var state_39168__$1 = state_39168;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_39168__$1,inst_39166);
} else {
if((state_val_39169 === (4))){
var inst_39157 = (state_39168[(8)]);
var inst_39157__$1 = (state_39168[(2)]);
var inst_39158 = (inst_39157__$1 == null);
var state_39168__$1 = (function (){var statearr_39172 = state_39168;
(statearr_39172[(8)] = inst_39157__$1);

return statearr_39172;
})();
if(cljs.core.truth_(inst_39158)){
var statearr_39173_39187 = state_39168__$1;
(statearr_39173_39187[(1)] = (5));

} else {
var statearr_39174_39188 = state_39168__$1;
(statearr_39174_39188[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39169 === (5))){
var inst_39154 = (state_39168[(7)]);
var state_39168__$1 = state_39168;
var statearr_39175_39189 = state_39168__$1;
(statearr_39175_39189[(2)] = inst_39154);

(statearr_39175_39189[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39169 === (6))){
var inst_39157 = (state_39168[(8)]);
var inst_39154 = (state_39168[(7)]);
var inst_39161 = f.call(null,inst_39154,inst_39157);
var inst_39154__$1 = inst_39161;
var state_39168__$1 = (function (){var statearr_39176 = state_39168;
(statearr_39176[(7)] = inst_39154__$1);

return statearr_39176;
})();
var statearr_39177_39190 = state_39168__$1;
(statearr_39177_39190[(2)] = null);

(statearr_39177_39190[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39169 === (7))){
var inst_39164 = (state_39168[(2)]);
var state_39168__$1 = state_39168;
var statearr_39178_39191 = state_39168__$1;
(statearr_39178_39191[(2)] = inst_39164);

(statearr_39178_39191[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var cljs$core$async$reduce_$_state_machine__23572__auto__ = null;
var cljs$core$async$reduce_$_state_machine__23572__auto____0 = (function (){
var statearr_39182 = [null,null,null,null,null,null,null,null,null];
(statearr_39182[(0)] = cljs$core$async$reduce_$_state_machine__23572__auto__);

(statearr_39182[(1)] = (1));

return statearr_39182;
});
var cljs$core$async$reduce_$_state_machine__23572__auto____1 = (function (state_39168){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_39168);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e39183){if((e39183 instanceof Object)){
var ex__23575__auto__ = e39183;
var statearr_39184_39192 = state_39168;
(statearr_39184_39192[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_39168);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e39183;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39193 = state_39168;
state_39168 = G__39193;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$reduce_$_state_machine__23572__auto__ = function(state_39168){
switch(arguments.length){
case 0:
return cljs$core$async$reduce_$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$reduce_$_state_machine__23572__auto____1.call(this,state_39168);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$reduce_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$reduce_$_state_machine__23572__auto____0;
cljs$core$async$reduce_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$reduce_$_state_machine__23572__auto____1;
return cljs$core$async$reduce_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_39185 = f__23634__auto__.call(null);
(statearr_39185[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_39185;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
/**
 * Puts the contents of coll into the supplied channel.
 * 
 * By default the channel will be closed after the items are copied,
 * but can be determined by the close? parameter.
 * 
 * Returns a channel which will close after the items are copied.
 */
cljs.core.async.onto_chan = (function cljs$core$async$onto_chan(){
var G__39195 = arguments.length;
switch (G__39195) {
case 2:
return cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$2 = (function (ch,coll){
return cljs.core.async.onto_chan.call(null,ch,coll,true);
});

cljs.core.async.onto_chan.cljs$core$IFn$_invoke$arity$3 = (function (ch,coll,close_QMARK_){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_39220){
var state_val_39221 = (state_39220[(1)]);
if((state_val_39221 === (7))){
var inst_39202 = (state_39220[(2)]);
var state_39220__$1 = state_39220;
var statearr_39222_39246 = state_39220__$1;
(statearr_39222_39246[(2)] = inst_39202);

(statearr_39222_39246[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (1))){
var inst_39196 = cljs.core.seq.call(null,coll);
var inst_39197 = inst_39196;
var state_39220__$1 = (function (){var statearr_39223 = state_39220;
(statearr_39223[(7)] = inst_39197);

return statearr_39223;
})();
var statearr_39224_39247 = state_39220__$1;
(statearr_39224_39247[(2)] = null);

(statearr_39224_39247[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (4))){
var inst_39197 = (state_39220[(7)]);
var inst_39200 = cljs.core.first.call(null,inst_39197);
var state_39220__$1 = state_39220;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_39220__$1,(7),ch,inst_39200);
} else {
if((state_val_39221 === (13))){
var inst_39214 = (state_39220[(2)]);
var state_39220__$1 = state_39220;
var statearr_39225_39248 = state_39220__$1;
(statearr_39225_39248[(2)] = inst_39214);

(statearr_39225_39248[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (6))){
var inst_39205 = (state_39220[(2)]);
var state_39220__$1 = state_39220;
if(cljs.core.truth_(inst_39205)){
var statearr_39226_39249 = state_39220__$1;
(statearr_39226_39249[(1)] = (8));

} else {
var statearr_39227_39250 = state_39220__$1;
(statearr_39227_39250[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (3))){
var inst_39218 = (state_39220[(2)]);
var state_39220__$1 = state_39220;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_39220__$1,inst_39218);
} else {
if((state_val_39221 === (12))){
var state_39220__$1 = state_39220;
var statearr_39228_39251 = state_39220__$1;
(statearr_39228_39251[(2)] = null);

(statearr_39228_39251[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (2))){
var inst_39197 = (state_39220[(7)]);
var state_39220__$1 = state_39220;
if(cljs.core.truth_(inst_39197)){
var statearr_39229_39252 = state_39220__$1;
(statearr_39229_39252[(1)] = (4));

} else {
var statearr_39230_39253 = state_39220__$1;
(statearr_39230_39253[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (11))){
var inst_39211 = cljs.core.async.close_BANG_.call(null,ch);
var state_39220__$1 = state_39220;
var statearr_39231_39254 = state_39220__$1;
(statearr_39231_39254[(2)] = inst_39211);

(statearr_39231_39254[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (9))){
var state_39220__$1 = state_39220;
if(cljs.core.truth_(close_QMARK_)){
var statearr_39232_39255 = state_39220__$1;
(statearr_39232_39255[(1)] = (11));

} else {
var statearr_39233_39256 = state_39220__$1;
(statearr_39233_39256[(1)] = (12));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (5))){
var inst_39197 = (state_39220[(7)]);
var state_39220__$1 = state_39220;
var statearr_39234_39257 = state_39220__$1;
(statearr_39234_39257[(2)] = inst_39197);

(statearr_39234_39257[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (10))){
var inst_39216 = (state_39220[(2)]);
var state_39220__$1 = state_39220;
var statearr_39235_39258 = state_39220__$1;
(statearr_39235_39258[(2)] = inst_39216);

(statearr_39235_39258[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39221 === (8))){
var inst_39197 = (state_39220[(7)]);
var inst_39207 = cljs.core.next.call(null,inst_39197);
var inst_39197__$1 = inst_39207;
var state_39220__$1 = (function (){var statearr_39236 = state_39220;
(statearr_39236[(7)] = inst_39197__$1);

return statearr_39236;
})();
var statearr_39237_39259 = state_39220__$1;
(statearr_39237_39259[(2)] = null);

(statearr_39237_39259[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_39241 = [null,null,null,null,null,null,null,null];
(statearr_39241[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_39241[(1)] = (1));

return statearr_39241;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_39220){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_39220);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e39242){if((e39242 instanceof Object)){
var ex__23575__auto__ = e39242;
var statearr_39243_39260 = state_39220;
(statearr_39243_39260[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_39220);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e39242;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39261 = state_39220;
state_39220 = G__39261;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_39220){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_39220);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_39244 = f__23634__auto__.call(null);
(statearr_39244[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_39244;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});

cljs.core.async.onto_chan.cljs$lang$maxFixedArity = 3;
/**
 * Creates and returns a channel which contains the contents of coll,
 * closing when exhausted.
 */
cljs.core.async.to_chan = (function cljs$core$async$to_chan(coll){
var ch = cljs.core.async.chan.call(null,cljs.core.bounded_count.call(null,(100),coll));
cljs.core.async.onto_chan.call(null,ch,coll);

return ch;
});

cljs.core.async.Mux = (function (){var obj39263 = {};
return obj39263;
})();

cljs.core.async.muxch_STAR_ = (function cljs$core$async$muxch_STAR_(_){
if((function (){var and__16057__auto__ = _;
if(and__16057__auto__){
return _.cljs$core$async$Mux$muxch_STAR_$arity$1;
} else {
return and__16057__auto__;
}
})()){
return _.cljs$core$async$Mux$muxch_STAR_$arity$1(_);
} else {
var x__16705__auto__ = (((_ == null))?null:_);
return (function (){var or__16069__auto__ = (cljs.core.async.muxch_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.muxch_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mux.muxch*",_);
}
}
})().call(null,_);
}
});


cljs.core.async.Mult = (function (){var obj39265 = {};
return obj39265;
})();

cljs.core.async.tap_STAR_ = (function cljs$core$async$tap_STAR_(m,ch,close_QMARK_){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mult$tap_STAR_$arity$3;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mult$tap_STAR_$arity$3(m,ch,close_QMARK_);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.tap_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.tap_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.tap*",m);
}
}
})().call(null,m,ch,close_QMARK_);
}
});

cljs.core.async.untap_STAR_ = (function cljs$core$async$untap_STAR_(m,ch){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mult$untap_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mult$untap_STAR_$arity$2(m,ch);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.untap_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.untap_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.untap*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.untap_all_STAR_ = (function cljs$core$async$untap_all_STAR_(m){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mult$untap_all_STAR_$arity$1;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mult$untap_all_STAR_$arity$1(m);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.untap_all_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.untap_all_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mult.untap-all*",m);
}
}
})().call(null,m);
}
});

/**
 * Creates and returns a mult(iple) of the supplied channel. Channels
 * containing copies of the channel can be created with 'tap', and
 * detached with 'untap'.
 * 
 * Each item is distributed to all taps in parallel and synchronously,
 * i.e. each tap must accept before the next item is distributed. Use
 * buffering/windowing to prevent slow taps from holding up the mult.
 * 
 * Items received when there are no taps get dropped.
 * 
 * If a tap puts to a closed channel, it will be removed from the mult.
 */
cljs.core.async.mult = (function cljs$core$async$mult(ch){
var cs = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var m = (function (){
if(typeof cljs.core.async.t39487 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t39487 = (function (mult,ch,cs,meta39488){
this.mult = mult;
this.ch = ch;
this.cs = cs;
this.meta39488 = meta39488;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t39487.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (cs){
return (function (_39489,meta39488__$1){
var self__ = this;
var _39489__$1 = this;
return (new cljs.core.async.t39487(self__.mult,self__.ch,self__.cs,meta39488__$1));
});})(cs))
;

cljs.core.async.t39487.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (cs){
return (function (_39489){
var self__ = this;
var _39489__$1 = this;
return self__.meta39488;
});})(cs))
;

cljs.core.async.t39487.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t39487.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (cs){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.ch;
});})(cs))
;

cljs.core.async.t39487.prototype.cljs$core$async$Mult$ = true;

cljs.core.async.t39487.prototype.cljs$core$async$Mult$tap_STAR_$arity$3 = ((function (cs){
return (function (_,ch__$1,close_QMARK_){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.assoc,ch__$1,close_QMARK_);

return null;
});})(cs))
;

cljs.core.async.t39487.prototype.cljs$core$async$Mult$untap_STAR_$arity$2 = ((function (cs){
return (function (_,ch__$1){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.dissoc,ch__$1);

return null;
});})(cs))
;

cljs.core.async.t39487.prototype.cljs$core$async$Mult$untap_all_STAR_$arity$1 = ((function (cs){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.cs,cljs.core.PersistentArrayMap.EMPTY);

return null;
});})(cs))
;

cljs.core.async.t39487.getBasis = ((function (cs){
return (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"mult","mult",-1187640995,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"cs","cs",-117024463,null),new cljs.core.Symbol(null,"meta39488","meta39488",1836682593,null)], null);
});})(cs))
;

cljs.core.async.t39487.cljs$lang$type = true;

cljs.core.async.t39487.cljs$lang$ctorStr = "cljs.core.async/t39487";

cljs.core.async.t39487.cljs$lang$ctorPrWriter = ((function (cs){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t39487");
});})(cs))
;

cljs.core.async.__GT_t39487 = ((function (cs){
return (function cljs$core$async$mult_$___GT_t39487(mult__$1,ch__$1,cs__$1,meta39488){
return (new cljs.core.async.t39487(mult__$1,ch__$1,cs__$1,meta39488));
});})(cs))
;

}

return (new cljs.core.async.t39487(cljs$core$async$mult,ch,cs,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var dchan = cljs.core.async.chan.call(null,(1));
var dctr = cljs.core.atom.call(null,null);
var done = ((function (cs,m,dchan,dctr){
return (function (_){
if((cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec) === (0))){
return cljs.core.async.put_BANG_.call(null,dchan,true);
} else {
return null;
}
});})(cs,m,dchan,dctr))
;
var c__23633__auto___39708 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___39708,cs,m,dchan,dctr,done){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___39708,cs,m,dchan,dctr,done){
return (function (state_39620){
var state_val_39621 = (state_39620[(1)]);
if((state_val_39621 === (7))){
var inst_39616 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39622_39709 = state_39620__$1;
(statearr_39622_39709[(2)] = inst_39616);

(statearr_39622_39709[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (20))){
var inst_39521 = (state_39620[(7)]);
var inst_39531 = cljs.core.first.call(null,inst_39521);
var inst_39532 = cljs.core.nth.call(null,inst_39531,(0),null);
var inst_39533 = cljs.core.nth.call(null,inst_39531,(1),null);
var state_39620__$1 = (function (){var statearr_39623 = state_39620;
(statearr_39623[(8)] = inst_39532);

return statearr_39623;
})();
if(cljs.core.truth_(inst_39533)){
var statearr_39624_39710 = state_39620__$1;
(statearr_39624_39710[(1)] = (22));

} else {
var statearr_39625_39711 = state_39620__$1;
(statearr_39625_39711[(1)] = (23));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (27))){
var inst_39563 = (state_39620[(9)]);
var inst_39492 = (state_39620[(10)]);
var inst_39561 = (state_39620[(11)]);
var inst_39568 = (state_39620[(12)]);
var inst_39568__$1 = cljs.core._nth.call(null,inst_39561,inst_39563);
var inst_39569 = cljs.core.async.put_BANG_.call(null,inst_39568__$1,inst_39492,done);
var state_39620__$1 = (function (){var statearr_39626 = state_39620;
(statearr_39626[(12)] = inst_39568__$1);

return statearr_39626;
})();
if(cljs.core.truth_(inst_39569)){
var statearr_39627_39712 = state_39620__$1;
(statearr_39627_39712[(1)] = (30));

} else {
var statearr_39628_39713 = state_39620__$1;
(statearr_39628_39713[(1)] = (31));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (1))){
var state_39620__$1 = state_39620;
var statearr_39629_39714 = state_39620__$1;
(statearr_39629_39714[(2)] = null);

(statearr_39629_39714[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (24))){
var inst_39521 = (state_39620[(7)]);
var inst_39538 = (state_39620[(2)]);
var inst_39539 = cljs.core.next.call(null,inst_39521);
var inst_39501 = inst_39539;
var inst_39502 = null;
var inst_39503 = (0);
var inst_39504 = (0);
var state_39620__$1 = (function (){var statearr_39630 = state_39620;
(statearr_39630[(13)] = inst_39501);

(statearr_39630[(14)] = inst_39504);

(statearr_39630[(15)] = inst_39503);

(statearr_39630[(16)] = inst_39502);

(statearr_39630[(17)] = inst_39538);

return statearr_39630;
})();
var statearr_39631_39715 = state_39620__$1;
(statearr_39631_39715[(2)] = null);

(statearr_39631_39715[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (39))){
var state_39620__$1 = state_39620;
var statearr_39635_39716 = state_39620__$1;
(statearr_39635_39716[(2)] = null);

(statearr_39635_39716[(1)] = (41));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (4))){
var inst_39492 = (state_39620[(10)]);
var inst_39492__$1 = (state_39620[(2)]);
var inst_39493 = (inst_39492__$1 == null);
var state_39620__$1 = (function (){var statearr_39636 = state_39620;
(statearr_39636[(10)] = inst_39492__$1);

return statearr_39636;
})();
if(cljs.core.truth_(inst_39493)){
var statearr_39637_39717 = state_39620__$1;
(statearr_39637_39717[(1)] = (5));

} else {
var statearr_39638_39718 = state_39620__$1;
(statearr_39638_39718[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (15))){
var inst_39501 = (state_39620[(13)]);
var inst_39504 = (state_39620[(14)]);
var inst_39503 = (state_39620[(15)]);
var inst_39502 = (state_39620[(16)]);
var inst_39517 = (state_39620[(2)]);
var inst_39518 = (inst_39504 + (1));
var tmp39632 = inst_39501;
var tmp39633 = inst_39503;
var tmp39634 = inst_39502;
var inst_39501__$1 = tmp39632;
var inst_39502__$1 = tmp39634;
var inst_39503__$1 = tmp39633;
var inst_39504__$1 = inst_39518;
var state_39620__$1 = (function (){var statearr_39639 = state_39620;
(statearr_39639[(13)] = inst_39501__$1);

(statearr_39639[(14)] = inst_39504__$1);

(statearr_39639[(15)] = inst_39503__$1);

(statearr_39639[(18)] = inst_39517);

(statearr_39639[(16)] = inst_39502__$1);

return statearr_39639;
})();
var statearr_39640_39719 = state_39620__$1;
(statearr_39640_39719[(2)] = null);

(statearr_39640_39719[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (21))){
var inst_39542 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39644_39720 = state_39620__$1;
(statearr_39644_39720[(2)] = inst_39542);

(statearr_39644_39720[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (31))){
var inst_39568 = (state_39620[(12)]);
var inst_39572 = done.call(null,null);
var inst_39573 = cljs.core.async.untap_STAR_.call(null,m,inst_39568);
var state_39620__$1 = (function (){var statearr_39645 = state_39620;
(statearr_39645[(19)] = inst_39572);

return statearr_39645;
})();
var statearr_39646_39721 = state_39620__$1;
(statearr_39646_39721[(2)] = inst_39573);

(statearr_39646_39721[(1)] = (32));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (32))){
var inst_39563 = (state_39620[(9)]);
var inst_39560 = (state_39620[(20)]);
var inst_39561 = (state_39620[(11)]);
var inst_39562 = (state_39620[(21)]);
var inst_39575 = (state_39620[(2)]);
var inst_39576 = (inst_39563 + (1));
var tmp39641 = inst_39560;
var tmp39642 = inst_39561;
var tmp39643 = inst_39562;
var inst_39560__$1 = tmp39641;
var inst_39561__$1 = tmp39642;
var inst_39562__$1 = tmp39643;
var inst_39563__$1 = inst_39576;
var state_39620__$1 = (function (){var statearr_39647 = state_39620;
(statearr_39647[(9)] = inst_39563__$1);

(statearr_39647[(22)] = inst_39575);

(statearr_39647[(20)] = inst_39560__$1);

(statearr_39647[(11)] = inst_39561__$1);

(statearr_39647[(21)] = inst_39562__$1);

return statearr_39647;
})();
var statearr_39648_39722 = state_39620__$1;
(statearr_39648_39722[(2)] = null);

(statearr_39648_39722[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (40))){
var inst_39588 = (state_39620[(23)]);
var inst_39592 = done.call(null,null);
var inst_39593 = cljs.core.async.untap_STAR_.call(null,m,inst_39588);
var state_39620__$1 = (function (){var statearr_39649 = state_39620;
(statearr_39649[(24)] = inst_39592);

return statearr_39649;
})();
var statearr_39650_39723 = state_39620__$1;
(statearr_39650_39723[(2)] = inst_39593);

(statearr_39650_39723[(1)] = (41));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (33))){
var inst_39579 = (state_39620[(25)]);
var inst_39581 = cljs.core.chunked_seq_QMARK_.call(null,inst_39579);
var state_39620__$1 = state_39620;
if(inst_39581){
var statearr_39651_39724 = state_39620__$1;
(statearr_39651_39724[(1)] = (36));

} else {
var statearr_39652_39725 = state_39620__$1;
(statearr_39652_39725[(1)] = (37));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (13))){
var inst_39511 = (state_39620[(26)]);
var inst_39514 = cljs.core.async.close_BANG_.call(null,inst_39511);
var state_39620__$1 = state_39620;
var statearr_39653_39726 = state_39620__$1;
(statearr_39653_39726[(2)] = inst_39514);

(statearr_39653_39726[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (22))){
var inst_39532 = (state_39620[(8)]);
var inst_39535 = cljs.core.async.close_BANG_.call(null,inst_39532);
var state_39620__$1 = state_39620;
var statearr_39654_39727 = state_39620__$1;
(statearr_39654_39727[(2)] = inst_39535);

(statearr_39654_39727[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (36))){
var inst_39579 = (state_39620[(25)]);
var inst_39583 = cljs.core.chunk_first.call(null,inst_39579);
var inst_39584 = cljs.core.chunk_rest.call(null,inst_39579);
var inst_39585 = cljs.core.count.call(null,inst_39583);
var inst_39560 = inst_39584;
var inst_39561 = inst_39583;
var inst_39562 = inst_39585;
var inst_39563 = (0);
var state_39620__$1 = (function (){var statearr_39655 = state_39620;
(statearr_39655[(9)] = inst_39563);

(statearr_39655[(20)] = inst_39560);

(statearr_39655[(11)] = inst_39561);

(statearr_39655[(21)] = inst_39562);

return statearr_39655;
})();
var statearr_39656_39728 = state_39620__$1;
(statearr_39656_39728[(2)] = null);

(statearr_39656_39728[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (41))){
var inst_39579 = (state_39620[(25)]);
var inst_39595 = (state_39620[(2)]);
var inst_39596 = cljs.core.next.call(null,inst_39579);
var inst_39560 = inst_39596;
var inst_39561 = null;
var inst_39562 = (0);
var inst_39563 = (0);
var state_39620__$1 = (function (){var statearr_39657 = state_39620;
(statearr_39657[(9)] = inst_39563);

(statearr_39657[(20)] = inst_39560);

(statearr_39657[(11)] = inst_39561);

(statearr_39657[(27)] = inst_39595);

(statearr_39657[(21)] = inst_39562);

return statearr_39657;
})();
var statearr_39658_39729 = state_39620__$1;
(statearr_39658_39729[(2)] = null);

(statearr_39658_39729[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (43))){
var state_39620__$1 = state_39620;
var statearr_39659_39730 = state_39620__$1;
(statearr_39659_39730[(2)] = null);

(statearr_39659_39730[(1)] = (44));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (29))){
var inst_39604 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39660_39731 = state_39620__$1;
(statearr_39660_39731[(2)] = inst_39604);

(statearr_39660_39731[(1)] = (26));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (44))){
var inst_39613 = (state_39620[(2)]);
var state_39620__$1 = (function (){var statearr_39661 = state_39620;
(statearr_39661[(28)] = inst_39613);

return statearr_39661;
})();
var statearr_39662_39732 = state_39620__$1;
(statearr_39662_39732[(2)] = null);

(statearr_39662_39732[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (6))){
var inst_39552 = (state_39620[(29)]);
var inst_39551 = cljs.core.deref.call(null,cs);
var inst_39552__$1 = cljs.core.keys.call(null,inst_39551);
var inst_39553 = cljs.core.count.call(null,inst_39552__$1);
var inst_39554 = cljs.core.reset_BANG_.call(null,dctr,inst_39553);
var inst_39559 = cljs.core.seq.call(null,inst_39552__$1);
var inst_39560 = inst_39559;
var inst_39561 = null;
var inst_39562 = (0);
var inst_39563 = (0);
var state_39620__$1 = (function (){var statearr_39663 = state_39620;
(statearr_39663[(9)] = inst_39563);

(statearr_39663[(20)] = inst_39560);

(statearr_39663[(11)] = inst_39561);

(statearr_39663[(29)] = inst_39552__$1);

(statearr_39663[(30)] = inst_39554);

(statearr_39663[(21)] = inst_39562);

return statearr_39663;
})();
var statearr_39664_39733 = state_39620__$1;
(statearr_39664_39733[(2)] = null);

(statearr_39664_39733[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (28))){
var inst_39579 = (state_39620[(25)]);
var inst_39560 = (state_39620[(20)]);
var inst_39579__$1 = cljs.core.seq.call(null,inst_39560);
var state_39620__$1 = (function (){var statearr_39665 = state_39620;
(statearr_39665[(25)] = inst_39579__$1);

return statearr_39665;
})();
if(inst_39579__$1){
var statearr_39666_39734 = state_39620__$1;
(statearr_39666_39734[(1)] = (33));

} else {
var statearr_39667_39735 = state_39620__$1;
(statearr_39667_39735[(1)] = (34));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (25))){
var inst_39563 = (state_39620[(9)]);
var inst_39562 = (state_39620[(21)]);
var inst_39565 = (inst_39563 < inst_39562);
var inst_39566 = inst_39565;
var state_39620__$1 = state_39620;
if(cljs.core.truth_(inst_39566)){
var statearr_39668_39736 = state_39620__$1;
(statearr_39668_39736[(1)] = (27));

} else {
var statearr_39669_39737 = state_39620__$1;
(statearr_39669_39737[(1)] = (28));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (34))){
var state_39620__$1 = state_39620;
var statearr_39670_39738 = state_39620__$1;
(statearr_39670_39738[(2)] = null);

(statearr_39670_39738[(1)] = (35));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (17))){
var state_39620__$1 = state_39620;
var statearr_39671_39739 = state_39620__$1;
(statearr_39671_39739[(2)] = null);

(statearr_39671_39739[(1)] = (18));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (3))){
var inst_39618 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_39620__$1,inst_39618);
} else {
if((state_val_39621 === (12))){
var inst_39547 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39672_39740 = state_39620__$1;
(statearr_39672_39740[(2)] = inst_39547);

(statearr_39672_39740[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (2))){
var state_39620__$1 = state_39620;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_39620__$1,(4),ch);
} else {
if((state_val_39621 === (23))){
var state_39620__$1 = state_39620;
var statearr_39673_39741 = state_39620__$1;
(statearr_39673_39741[(2)] = null);

(statearr_39673_39741[(1)] = (24));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (35))){
var inst_39602 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39674_39742 = state_39620__$1;
(statearr_39674_39742[(2)] = inst_39602);

(statearr_39674_39742[(1)] = (29));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (19))){
var inst_39521 = (state_39620[(7)]);
var inst_39525 = cljs.core.chunk_first.call(null,inst_39521);
var inst_39526 = cljs.core.chunk_rest.call(null,inst_39521);
var inst_39527 = cljs.core.count.call(null,inst_39525);
var inst_39501 = inst_39526;
var inst_39502 = inst_39525;
var inst_39503 = inst_39527;
var inst_39504 = (0);
var state_39620__$1 = (function (){var statearr_39675 = state_39620;
(statearr_39675[(13)] = inst_39501);

(statearr_39675[(14)] = inst_39504);

(statearr_39675[(15)] = inst_39503);

(statearr_39675[(16)] = inst_39502);

return statearr_39675;
})();
var statearr_39676_39743 = state_39620__$1;
(statearr_39676_39743[(2)] = null);

(statearr_39676_39743[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (11))){
var inst_39521 = (state_39620[(7)]);
var inst_39501 = (state_39620[(13)]);
var inst_39521__$1 = cljs.core.seq.call(null,inst_39501);
var state_39620__$1 = (function (){var statearr_39677 = state_39620;
(statearr_39677[(7)] = inst_39521__$1);

return statearr_39677;
})();
if(inst_39521__$1){
var statearr_39678_39744 = state_39620__$1;
(statearr_39678_39744[(1)] = (16));

} else {
var statearr_39679_39745 = state_39620__$1;
(statearr_39679_39745[(1)] = (17));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (9))){
var inst_39549 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39680_39746 = state_39620__$1;
(statearr_39680_39746[(2)] = inst_39549);

(statearr_39680_39746[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (5))){
var inst_39499 = cljs.core.deref.call(null,cs);
var inst_39500 = cljs.core.seq.call(null,inst_39499);
var inst_39501 = inst_39500;
var inst_39502 = null;
var inst_39503 = (0);
var inst_39504 = (0);
var state_39620__$1 = (function (){var statearr_39681 = state_39620;
(statearr_39681[(13)] = inst_39501);

(statearr_39681[(14)] = inst_39504);

(statearr_39681[(15)] = inst_39503);

(statearr_39681[(16)] = inst_39502);

return statearr_39681;
})();
var statearr_39682_39747 = state_39620__$1;
(statearr_39682_39747[(2)] = null);

(statearr_39682_39747[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (14))){
var state_39620__$1 = state_39620;
var statearr_39683_39748 = state_39620__$1;
(statearr_39683_39748[(2)] = null);

(statearr_39683_39748[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (45))){
var inst_39610 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39684_39749 = state_39620__$1;
(statearr_39684_39749[(2)] = inst_39610);

(statearr_39684_39749[(1)] = (44));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (26))){
var inst_39552 = (state_39620[(29)]);
var inst_39606 = (state_39620[(2)]);
var inst_39607 = cljs.core.seq.call(null,inst_39552);
var state_39620__$1 = (function (){var statearr_39685 = state_39620;
(statearr_39685[(31)] = inst_39606);

return statearr_39685;
})();
if(inst_39607){
var statearr_39686_39750 = state_39620__$1;
(statearr_39686_39750[(1)] = (42));

} else {
var statearr_39687_39751 = state_39620__$1;
(statearr_39687_39751[(1)] = (43));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (16))){
var inst_39521 = (state_39620[(7)]);
var inst_39523 = cljs.core.chunked_seq_QMARK_.call(null,inst_39521);
var state_39620__$1 = state_39620;
if(inst_39523){
var statearr_39688_39752 = state_39620__$1;
(statearr_39688_39752[(1)] = (19));

} else {
var statearr_39689_39753 = state_39620__$1;
(statearr_39689_39753[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (38))){
var inst_39599 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39690_39754 = state_39620__$1;
(statearr_39690_39754[(2)] = inst_39599);

(statearr_39690_39754[(1)] = (35));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (30))){
var state_39620__$1 = state_39620;
var statearr_39691_39755 = state_39620__$1;
(statearr_39691_39755[(2)] = null);

(statearr_39691_39755[(1)] = (32));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (10))){
var inst_39504 = (state_39620[(14)]);
var inst_39502 = (state_39620[(16)]);
var inst_39510 = cljs.core._nth.call(null,inst_39502,inst_39504);
var inst_39511 = cljs.core.nth.call(null,inst_39510,(0),null);
var inst_39512 = cljs.core.nth.call(null,inst_39510,(1),null);
var state_39620__$1 = (function (){var statearr_39692 = state_39620;
(statearr_39692[(26)] = inst_39511);

return statearr_39692;
})();
if(cljs.core.truth_(inst_39512)){
var statearr_39693_39756 = state_39620__$1;
(statearr_39693_39756[(1)] = (13));

} else {
var statearr_39694_39757 = state_39620__$1;
(statearr_39694_39757[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (18))){
var inst_39545 = (state_39620[(2)]);
var state_39620__$1 = state_39620;
var statearr_39695_39758 = state_39620__$1;
(statearr_39695_39758[(2)] = inst_39545);

(statearr_39695_39758[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (42))){
var state_39620__$1 = state_39620;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_39620__$1,(45),dchan);
} else {
if((state_val_39621 === (37))){
var inst_39579 = (state_39620[(25)]);
var inst_39588 = (state_39620[(23)]);
var inst_39492 = (state_39620[(10)]);
var inst_39588__$1 = cljs.core.first.call(null,inst_39579);
var inst_39589 = cljs.core.async.put_BANG_.call(null,inst_39588__$1,inst_39492,done);
var state_39620__$1 = (function (){var statearr_39696 = state_39620;
(statearr_39696[(23)] = inst_39588__$1);

return statearr_39696;
})();
if(cljs.core.truth_(inst_39589)){
var statearr_39697_39759 = state_39620__$1;
(statearr_39697_39759[(1)] = (39));

} else {
var statearr_39698_39760 = state_39620__$1;
(statearr_39698_39760[(1)] = (40));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39621 === (8))){
var inst_39504 = (state_39620[(14)]);
var inst_39503 = (state_39620[(15)]);
var inst_39506 = (inst_39504 < inst_39503);
var inst_39507 = inst_39506;
var state_39620__$1 = state_39620;
if(cljs.core.truth_(inst_39507)){
var statearr_39699_39761 = state_39620__$1;
(statearr_39699_39761[(1)] = (10));

} else {
var statearr_39700_39762 = state_39620__$1;
(statearr_39700_39762[(1)] = (11));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___39708,cs,m,dchan,dctr,done))
;
return ((function (switch__23571__auto__,c__23633__auto___39708,cs,m,dchan,dctr,done){
return (function() {
var cljs$core$async$mult_$_state_machine__23572__auto__ = null;
var cljs$core$async$mult_$_state_machine__23572__auto____0 = (function (){
var statearr_39704 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_39704[(0)] = cljs$core$async$mult_$_state_machine__23572__auto__);

(statearr_39704[(1)] = (1));

return statearr_39704;
});
var cljs$core$async$mult_$_state_machine__23572__auto____1 = (function (state_39620){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_39620);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e39705){if((e39705 instanceof Object)){
var ex__23575__auto__ = e39705;
var statearr_39706_39763 = state_39620;
(statearr_39706_39763[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_39620);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e39705;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__39764 = state_39620;
state_39620 = G__39764;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$mult_$_state_machine__23572__auto__ = function(state_39620){
switch(arguments.length){
case 0:
return cljs$core$async$mult_$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$mult_$_state_machine__23572__auto____1.call(this,state_39620);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mult_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mult_$_state_machine__23572__auto____0;
cljs$core$async$mult_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mult_$_state_machine__23572__auto____1;
return cljs$core$async$mult_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___39708,cs,m,dchan,dctr,done))
})();
var state__23635__auto__ = (function (){var statearr_39707 = f__23634__auto__.call(null);
(statearr_39707[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___39708);

return statearr_39707;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___39708,cs,m,dchan,dctr,done))
);


return m;
});
/**
 * Copies the mult source onto the supplied channel.
 * 
 * By default the channel will be closed when the source closes,
 * but can be determined by the close? parameter.
 */
cljs.core.async.tap = (function cljs$core$async$tap(){
var G__39766 = arguments.length;
switch (G__39766) {
case 2:
return cljs.core.async.tap.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.tap.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.tap.cljs$core$IFn$_invoke$arity$2 = (function (mult,ch){
return cljs.core.async.tap.call(null,mult,ch,true);
});

cljs.core.async.tap.cljs$core$IFn$_invoke$arity$3 = (function (mult,ch,close_QMARK_){
cljs.core.async.tap_STAR_.call(null,mult,ch,close_QMARK_);

return ch;
});

cljs.core.async.tap.cljs$lang$maxFixedArity = 3;
/**
 * Disconnects a target channel from a mult
 */
cljs.core.async.untap = (function cljs$core$async$untap(mult,ch){
return cljs.core.async.untap_STAR_.call(null,mult,ch);
});
/**
 * Disconnects all target channels from a mult
 */
cljs.core.async.untap_all = (function cljs$core$async$untap_all(mult){
return cljs.core.async.untap_all_STAR_.call(null,mult);
});

cljs.core.async.Mix = (function (){var obj39769 = {};
return obj39769;
})();

cljs.core.async.admix_STAR_ = (function cljs$core$async$admix_STAR_(m,ch){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mix$admix_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mix$admix_STAR_$arity$2(m,ch);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.admix_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.admix_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.admix*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.unmix_STAR_ = (function cljs$core$async$unmix_STAR_(m,ch){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mix$unmix_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mix$unmix_STAR_$arity$2(m,ch);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.unmix_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.unmix_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.unmix*",m);
}
}
})().call(null,m,ch);
}
});

cljs.core.async.unmix_all_STAR_ = (function cljs$core$async$unmix_all_STAR_(m){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mix$unmix_all_STAR_$arity$1;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mix$unmix_all_STAR_$arity$1(m);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.unmix_all_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.unmix_all_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.unmix-all*",m);
}
}
})().call(null,m);
}
});

cljs.core.async.toggle_STAR_ = (function cljs$core$async$toggle_STAR_(m,state_map){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mix$toggle_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mix$toggle_STAR_$arity$2(m,state_map);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.toggle_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.toggle_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.toggle*",m);
}
}
})().call(null,m,state_map);
}
});

cljs.core.async.solo_mode_STAR_ = (function cljs$core$async$solo_mode_STAR_(m,mode){
if((function (){var and__16057__auto__ = m;
if(and__16057__auto__){
return m.cljs$core$async$Mix$solo_mode_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return m.cljs$core$async$Mix$solo_mode_STAR_$arity$2(m,mode);
} else {
var x__16705__auto__ = (((m == null))?null:m);
return (function (){var or__16069__auto__ = (cljs.core.async.solo_mode_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.solo_mode_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Mix.solo-mode*",m);
}
}
})().call(null,m,mode);
}
});

cljs.core.async.ioc_alts_BANG_ = (function cljs$core$async$ioc_alts_BANG_(){
var argseq__17109__auto__ = ((((3) < arguments.length))?(new cljs.core.IndexedSeq(Array.prototype.slice.call(arguments,(3)),(0))):null);
return cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),argseq__17109__auto__);
});

cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic = (function (state,cont_block,ports,p__39774){
var map__39775 = p__39774;
var map__39775__$1 = ((cljs.core.seq_QMARK_.call(null,map__39775))?cljs.core.apply.call(null,cljs.core.hash_map,map__39775):map__39775);
var opts = map__39775__$1;
var statearr_39776_39779 = state;
(statearr_39776_39779[cljs.core.async.impl.ioc_helpers.STATE_IDX] = cont_block);


var temp__4425__auto__ = cljs.core.async.do_alts.call(null,((function (map__39775,map__39775__$1,opts){
return (function (val){
var statearr_39777_39780 = state;
(statearr_39777_39780[cljs.core.async.impl.ioc_helpers.VALUE_IDX] = val);


return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state);
});})(map__39775,map__39775__$1,opts))
,ports,opts);
if(cljs.core.truth_(temp__4425__auto__)){
var cb = temp__4425__auto__;
var statearr_39778_39781 = state;
(statearr_39778_39781[cljs.core.async.impl.ioc_helpers.VALUE_IDX] = cljs.core.deref.call(null,cb));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
});

cljs.core.async.ioc_alts_BANG_.cljs$lang$maxFixedArity = (3);

cljs.core.async.ioc_alts_BANG_.cljs$lang$applyTo = (function (seq39770){
var G__39771 = cljs.core.first.call(null,seq39770);
var seq39770__$1 = cljs.core.next.call(null,seq39770);
var G__39772 = cljs.core.first.call(null,seq39770__$1);
var seq39770__$2 = cljs.core.next.call(null,seq39770__$1);
var G__39773 = cljs.core.first.call(null,seq39770__$2);
var seq39770__$3 = cljs.core.next.call(null,seq39770__$2);
return cljs.core.async.ioc_alts_BANG_.cljs$core$IFn$_invoke$arity$variadic(G__39771,G__39772,G__39773,seq39770__$3);
});
/**
 * Creates and returns a mix of one or more input channels which will
 * be put on the supplied out channel. Input sources can be added to
 * the mix with 'admix', and removed with 'unmix'. A mix supports
 * soloing, muting and pausing multiple inputs atomically using
 * 'toggle', and can solo using either muting or pausing as determined
 * by 'solo-mode'.
 * 
 * Each channel can have zero or more boolean modes set via 'toggle':
 * 
 * :solo - when true, only this (ond other soloed) channel(s) will appear
 * in the mix output channel. :mute and :pause states of soloed
 * channels are ignored. If solo-mode is :mute, non-soloed
 * channels are muted, if :pause, non-soloed channels are
 * paused.
 * 
 * :mute - muted channels will have their contents consumed but not included in the mix
 * :pause - paused channels will not have their contents consumed (and thus also not included in the mix)
 */
cljs.core.async.mix = (function cljs$core$async$mix(out){
var cs = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var solo_modes = new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"pause","pause",-2095325672),null,new cljs.core.Keyword(null,"mute","mute",1151223646),null], null), null);
var attrs = cljs.core.conj.call(null,solo_modes,new cljs.core.Keyword(null,"solo","solo",-316350075));
var solo_mode = cljs.core.atom.call(null,new cljs.core.Keyword(null,"mute","mute",1151223646));
var change = cljs.core.async.chan.call(null);
var changed = ((function (cs,solo_modes,attrs,solo_mode,change){
return (function (){
return cljs.core.async.put_BANG_.call(null,change,true);
});})(cs,solo_modes,attrs,solo_mode,change))
;
var pick = ((function (cs,solo_modes,attrs,solo_mode,change,changed){
return (function (attr,chs){
return cljs.core.reduce_kv.call(null,((function (cs,solo_modes,attrs,solo_mode,change,changed){
return (function (ret,c,v){
if(cljs.core.truth_(attr.call(null,v))){
return cljs.core.conj.call(null,ret,c);
} else {
return ret;
}
});})(cs,solo_modes,attrs,solo_mode,change,changed))
,cljs.core.PersistentHashSet.EMPTY,chs);
});})(cs,solo_modes,attrs,solo_mode,change,changed))
;
var calc_state = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick){
return (function (){
var chs = cljs.core.deref.call(null,cs);
var mode = cljs.core.deref.call(null,solo_mode);
var solos = pick.call(null,new cljs.core.Keyword(null,"solo","solo",-316350075),chs);
var pauses = pick.call(null,new cljs.core.Keyword(null,"pause","pause",-2095325672),chs);
return new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"solos","solos",1441458643),solos,new cljs.core.Keyword(null,"mutes","mutes",1068806309),pick.call(null,new cljs.core.Keyword(null,"mute","mute",1151223646),chs),new cljs.core.Keyword(null,"reads","reads",-1215067361),cljs.core.conj.call(null,(((cljs.core._EQ_.call(null,mode,new cljs.core.Keyword(null,"pause","pause",-2095325672))) && (!(cljs.core.empty_QMARK_.call(null,solos))))?cljs.core.vec.call(null,solos):cljs.core.vec.call(null,cljs.core.remove.call(null,pauses,cljs.core.keys.call(null,chs)))),change)], null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick))
;
var m = (function (){
if(typeof cljs.core.async.t39901 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t39901 = (function (change,mix,solo_mode,pick,cs,calc_state,out,changed,solo_modes,attrs,meta39902){
this.change = change;
this.mix = mix;
this.solo_mode = solo_mode;
this.pick = pick;
this.cs = cs;
this.calc_state = calc_state;
this.out = out;
this.changed = changed;
this.solo_modes = solo_modes;
this.attrs = attrs;
this.meta39902 = meta39902;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t39901.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_39903,meta39902__$1){
var self__ = this;
var _39903__$1 = this;
return (new cljs.core.async.t39901(self__.change,self__.mix,self__.solo_mode,self__.pick,self__.cs,self__.calc_state,self__.out,self__.changed,self__.solo_modes,self__.attrs,meta39902__$1));
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_39903){
var self__ = this;
var _39903__$1 = this;
return self__.meta39902;
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t39901.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.out;
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$ = true;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$admix_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,ch){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.assoc,ch,cljs.core.PersistentArrayMap.EMPTY);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$unmix_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,ch){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.dissoc,ch);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$unmix_all_STAR_$arity$1 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_){
var self__ = this;
var ___$1 = this;
cljs.core.reset_BANG_.call(null,self__.cs,cljs.core.PersistentArrayMap.EMPTY);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$toggle_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,state_map){
var self__ = this;
var ___$1 = this;
cljs.core.swap_BANG_.call(null,self__.cs,cljs.core.partial.call(null,cljs.core.merge_with,cljs.core.merge),state_map);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.prototype.cljs$core$async$Mix$solo_mode_STAR_$arity$2 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (_,mode){
var self__ = this;
var ___$1 = this;
if(cljs.core.truth_(self__.solo_modes.call(null,mode))){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str([cljs.core.str("mode must be one of: "),cljs.core.str(self__.solo_modes)].join('')),cljs.core.str("\n"),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol(null,"solo-modes","solo-modes",882180540,null),new cljs.core.Symbol(null,"mode","mode",-2000032078,null))))].join('')));
}

cljs.core.reset_BANG_.call(null,self__.solo_mode,mode);

return self__.changed.call(null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.getBasis = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (){
return new cljs.core.PersistentVector(null, 11, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"change","change",477485025,null),new cljs.core.Symbol(null,"mix","mix",2121373763,null),new cljs.core.Symbol(null,"solo-mode","solo-mode",2031788074,null),new cljs.core.Symbol(null,"pick","pick",1300068175,null),new cljs.core.Symbol(null,"cs","cs",-117024463,null),new cljs.core.Symbol(null,"calc-state","calc-state",-349968968,null),new cljs.core.Symbol(null,"out","out",729986010,null),new cljs.core.Symbol(null,"changed","changed",-2083710852,null),new cljs.core.Symbol(null,"solo-modes","solo-modes",882180540,null),new cljs.core.Symbol(null,"attrs","attrs",-450137186,null),new cljs.core.Symbol(null,"meta39902","meta39902",-525504532,null)], null);
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.t39901.cljs$lang$type = true;

cljs.core.async.t39901.cljs$lang$ctorStr = "cljs.core.async/t39901";

cljs.core.async.t39901.cljs$lang$ctorPrWriter = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t39901");
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

cljs.core.async.__GT_t39901 = ((function (cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state){
return (function cljs$core$async$mix_$___GT_t39901(change__$1,mix__$1,solo_mode__$1,pick__$1,cs__$1,calc_state__$1,out__$1,changed__$1,solo_modes__$1,attrs__$1,meta39902){
return (new cljs.core.async.t39901(change__$1,mix__$1,solo_mode__$1,pick__$1,cs__$1,calc_state__$1,out__$1,changed__$1,solo_modes__$1,attrs__$1,meta39902));
});})(cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state))
;

}

return (new cljs.core.async.t39901(change,cljs$core$async$mix,solo_mode,pick,cs,calc_state,out,changed,solo_modes,attrs,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var c__23633__auto___40020 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function (state_39973){
var state_val_39974 = (state_39973[(1)]);
if((state_val_39974 === (7))){
var inst_39917 = (state_39973[(7)]);
var inst_39922 = cljs.core.apply.call(null,cljs.core.hash_map,inst_39917);
var state_39973__$1 = state_39973;
var statearr_39975_40021 = state_39973__$1;
(statearr_39975_40021[(2)] = inst_39922);

(statearr_39975_40021[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (20))){
var inst_39932 = (state_39973[(8)]);
var state_39973__$1 = state_39973;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_39973__$1,(23),out,inst_39932);
} else {
if((state_val_39974 === (1))){
var inst_39907 = (state_39973[(9)]);
var inst_39907__$1 = calc_state.call(null);
var inst_39908 = cljs.core.seq_QMARK_.call(null,inst_39907__$1);
var state_39973__$1 = (function (){var statearr_39976 = state_39973;
(statearr_39976[(9)] = inst_39907__$1);

return statearr_39976;
})();
if(inst_39908){
var statearr_39977_40022 = state_39973__$1;
(statearr_39977_40022[(1)] = (2));

} else {
var statearr_39978_40023 = state_39973__$1;
(statearr_39978_40023[(1)] = (3));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (24))){
var inst_39925 = (state_39973[(10)]);
var inst_39917 = inst_39925;
var state_39973__$1 = (function (){var statearr_39979 = state_39973;
(statearr_39979[(7)] = inst_39917);

return statearr_39979;
})();
var statearr_39980_40024 = state_39973__$1;
(statearr_39980_40024[(2)] = null);

(statearr_39980_40024[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (4))){
var inst_39907 = (state_39973[(9)]);
var inst_39913 = (state_39973[(2)]);
var inst_39914 = cljs.core.get.call(null,inst_39913,new cljs.core.Keyword(null,"solos","solos",1441458643));
var inst_39915 = cljs.core.get.call(null,inst_39913,new cljs.core.Keyword(null,"mutes","mutes",1068806309));
var inst_39916 = cljs.core.get.call(null,inst_39913,new cljs.core.Keyword(null,"reads","reads",-1215067361));
var inst_39917 = inst_39907;
var state_39973__$1 = (function (){var statearr_39981 = state_39973;
(statearr_39981[(11)] = inst_39915);

(statearr_39981[(7)] = inst_39917);

(statearr_39981[(12)] = inst_39914);

(statearr_39981[(13)] = inst_39916);

return statearr_39981;
})();
var statearr_39982_40025 = state_39973__$1;
(statearr_39982_40025[(2)] = null);

(statearr_39982_40025[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (15))){
var state_39973__$1 = state_39973;
var statearr_39983_40026 = state_39973__$1;
(statearr_39983_40026[(2)] = null);

(statearr_39983_40026[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (21))){
var inst_39925 = (state_39973[(10)]);
var inst_39917 = inst_39925;
var state_39973__$1 = (function (){var statearr_39984 = state_39973;
(statearr_39984[(7)] = inst_39917);

return statearr_39984;
})();
var statearr_39985_40027 = state_39973__$1;
(statearr_39985_40027[(2)] = null);

(statearr_39985_40027[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (13))){
var inst_39969 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
var statearr_39986_40028 = state_39973__$1;
(statearr_39986_40028[(2)] = inst_39969);

(statearr_39986_40028[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (22))){
var inst_39967 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
var statearr_39987_40029 = state_39973__$1;
(statearr_39987_40029[(2)] = inst_39967);

(statearr_39987_40029[(1)] = (13));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (6))){
var inst_39971 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_39973__$1,inst_39971);
} else {
if((state_val_39974 === (25))){
var state_39973__$1 = state_39973;
var statearr_39988_40030 = state_39973__$1;
(statearr_39988_40030[(2)] = null);

(statearr_39988_40030[(1)] = (26));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (17))){
var inst_39947 = (state_39973[(14)]);
var state_39973__$1 = state_39973;
var statearr_39989_40031 = state_39973__$1;
(statearr_39989_40031[(2)] = inst_39947);

(statearr_39989_40031[(1)] = (19));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (3))){
var inst_39907 = (state_39973[(9)]);
var state_39973__$1 = state_39973;
var statearr_39990_40032 = state_39973__$1;
(statearr_39990_40032[(2)] = inst_39907);

(statearr_39990_40032[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (12))){
var inst_39933 = (state_39973[(15)]);
var inst_39947 = (state_39973[(14)]);
var inst_39926 = (state_39973[(16)]);
var inst_39947__$1 = inst_39926.call(null,inst_39933);
var state_39973__$1 = (function (){var statearr_39991 = state_39973;
(statearr_39991[(14)] = inst_39947__$1);

return statearr_39991;
})();
if(cljs.core.truth_(inst_39947__$1)){
var statearr_39992_40033 = state_39973__$1;
(statearr_39992_40033[(1)] = (17));

} else {
var statearr_39993_40034 = state_39973__$1;
(statearr_39993_40034[(1)] = (18));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (2))){
var inst_39907 = (state_39973[(9)]);
var inst_39910 = cljs.core.apply.call(null,cljs.core.hash_map,inst_39907);
var state_39973__$1 = state_39973;
var statearr_39994_40035 = state_39973__$1;
(statearr_39994_40035[(2)] = inst_39910);

(statearr_39994_40035[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (23))){
var inst_39958 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
if(cljs.core.truth_(inst_39958)){
var statearr_39995_40036 = state_39973__$1;
(statearr_39995_40036[(1)] = (24));

} else {
var statearr_39996_40037 = state_39973__$1;
(statearr_39996_40037[(1)] = (25));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (19))){
var inst_39955 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
if(cljs.core.truth_(inst_39955)){
var statearr_39997_40038 = state_39973__$1;
(statearr_39997_40038[(1)] = (20));

} else {
var statearr_39998_40039 = state_39973__$1;
(statearr_39998_40039[(1)] = (21));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (11))){
var inst_39932 = (state_39973[(8)]);
var inst_39938 = (inst_39932 == null);
var state_39973__$1 = state_39973;
if(cljs.core.truth_(inst_39938)){
var statearr_39999_40040 = state_39973__$1;
(statearr_39999_40040[(1)] = (14));

} else {
var statearr_40000_40041 = state_39973__$1;
(statearr_40000_40041[(1)] = (15));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (9))){
var inst_39925 = (state_39973[(10)]);
var inst_39925__$1 = (state_39973[(2)]);
var inst_39926 = cljs.core.get.call(null,inst_39925__$1,new cljs.core.Keyword(null,"solos","solos",1441458643));
var inst_39927 = cljs.core.get.call(null,inst_39925__$1,new cljs.core.Keyword(null,"mutes","mutes",1068806309));
var inst_39928 = cljs.core.get.call(null,inst_39925__$1,new cljs.core.Keyword(null,"reads","reads",-1215067361));
var state_39973__$1 = (function (){var statearr_40001 = state_39973;
(statearr_40001[(17)] = inst_39927);

(statearr_40001[(10)] = inst_39925__$1);

(statearr_40001[(16)] = inst_39926);

return statearr_40001;
})();
return cljs.core.async.ioc_alts_BANG_.call(null,state_39973__$1,(10),inst_39928);
} else {
if((state_val_39974 === (5))){
var inst_39917 = (state_39973[(7)]);
var inst_39920 = cljs.core.seq_QMARK_.call(null,inst_39917);
var state_39973__$1 = state_39973;
if(inst_39920){
var statearr_40002_40042 = state_39973__$1;
(statearr_40002_40042[(1)] = (7));

} else {
var statearr_40003_40043 = state_39973__$1;
(statearr_40003_40043[(1)] = (8));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (14))){
var inst_39933 = (state_39973[(15)]);
var inst_39940 = cljs.core.swap_BANG_.call(null,cs,cljs.core.dissoc,inst_39933);
var state_39973__$1 = state_39973;
var statearr_40004_40044 = state_39973__$1;
(statearr_40004_40044[(2)] = inst_39940);

(statearr_40004_40044[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (26))){
var inst_39963 = (state_39973[(2)]);
var state_39973__$1 = state_39973;
var statearr_40005_40045 = state_39973__$1;
(statearr_40005_40045[(2)] = inst_39963);

(statearr_40005_40045[(1)] = (22));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (16))){
var inst_39943 = (state_39973[(2)]);
var inst_39944 = calc_state.call(null);
var inst_39917 = inst_39944;
var state_39973__$1 = (function (){var statearr_40006 = state_39973;
(statearr_40006[(7)] = inst_39917);

(statearr_40006[(18)] = inst_39943);

return statearr_40006;
})();
var statearr_40007_40046 = state_39973__$1;
(statearr_40007_40046[(2)] = null);

(statearr_40007_40046[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (10))){
var inst_39933 = (state_39973[(15)]);
var inst_39932 = (state_39973[(8)]);
var inst_39931 = (state_39973[(2)]);
var inst_39932__$1 = cljs.core.nth.call(null,inst_39931,(0),null);
var inst_39933__$1 = cljs.core.nth.call(null,inst_39931,(1),null);
var inst_39934 = (inst_39932__$1 == null);
var inst_39935 = cljs.core._EQ_.call(null,inst_39933__$1,change);
var inst_39936 = (inst_39934) || (inst_39935);
var state_39973__$1 = (function (){var statearr_40008 = state_39973;
(statearr_40008[(15)] = inst_39933__$1);

(statearr_40008[(8)] = inst_39932__$1);

return statearr_40008;
})();
if(cljs.core.truth_(inst_39936)){
var statearr_40009_40047 = state_39973__$1;
(statearr_40009_40047[(1)] = (11));

} else {
var statearr_40010_40048 = state_39973__$1;
(statearr_40010_40048[(1)] = (12));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (18))){
var inst_39927 = (state_39973[(17)]);
var inst_39933 = (state_39973[(15)]);
var inst_39926 = (state_39973[(16)]);
var inst_39950 = cljs.core.empty_QMARK_.call(null,inst_39926);
var inst_39951 = inst_39927.call(null,inst_39933);
var inst_39952 = cljs.core.not.call(null,inst_39951);
var inst_39953 = (inst_39950) && (inst_39952);
var state_39973__$1 = state_39973;
var statearr_40011_40049 = state_39973__$1;
(statearr_40011_40049[(2)] = inst_39953);

(statearr_40011_40049[(1)] = (19));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_39974 === (8))){
var inst_39917 = (state_39973[(7)]);
var state_39973__$1 = state_39973;
var statearr_40012_40050 = state_39973__$1;
(statearr_40012_40050[(2)] = inst_39917);

(statearr_40012_40050[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
;
return ((function (switch__23571__auto__,c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m){
return (function() {
var cljs$core$async$mix_$_state_machine__23572__auto__ = null;
var cljs$core$async$mix_$_state_machine__23572__auto____0 = (function (){
var statearr_40016 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40016[(0)] = cljs$core$async$mix_$_state_machine__23572__auto__);

(statearr_40016[(1)] = (1));

return statearr_40016;
});
var cljs$core$async$mix_$_state_machine__23572__auto____1 = (function (state_39973){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_39973);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40017){if((e40017 instanceof Object)){
var ex__23575__auto__ = e40017;
var statearr_40018_40051 = state_39973;
(statearr_40018_40051[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_39973);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40017;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40052 = state_39973;
state_39973 = G__40052;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$mix_$_state_machine__23572__auto__ = function(state_39973){
switch(arguments.length){
case 0:
return cljs$core$async$mix_$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$mix_$_state_machine__23572__auto____1.call(this,state_39973);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mix_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mix_$_state_machine__23572__auto____0;
cljs$core$async$mix_$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mix_$_state_machine__23572__auto____1;
return cljs$core$async$mix_$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
})();
var state__23635__auto__ = (function (){var statearr_40019 = f__23634__auto__.call(null);
(statearr_40019[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40020);

return statearr_40019;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40020,cs,solo_modes,attrs,solo_mode,change,changed,pick,calc_state,m))
);


return m;
});
/**
 * Adds ch as an input to the mix
 */
cljs.core.async.admix = (function cljs$core$async$admix(mix,ch){
return cljs.core.async.admix_STAR_.call(null,mix,ch);
});
/**
 * Removes ch as an input to the mix
 */
cljs.core.async.unmix = (function cljs$core$async$unmix(mix,ch){
return cljs.core.async.unmix_STAR_.call(null,mix,ch);
});
/**
 * removes all inputs from the mix
 */
cljs.core.async.unmix_all = (function cljs$core$async$unmix_all(mix){
return cljs.core.async.unmix_all_STAR_.call(null,mix);
});
/**
 * Atomically sets the state(s) of one or more channels in a mix. The
 * state map is a map of channels -> channel-state-map. A
 * channel-state-map is a map of attrs -> boolean, where attr is one or
 * more of :mute, :pause or :solo. Any states supplied are merged with
 * the current state.
 * 
 * Note that channels can be added to a mix via toggle, which can be
 * used to add channels in a particular (e.g. paused) state.
 */
cljs.core.async.toggle = (function cljs$core$async$toggle(mix,state_map){
return cljs.core.async.toggle_STAR_.call(null,mix,state_map);
});
/**
 * Sets the solo mode of the mix. mode must be one of :mute or :pause
 */
cljs.core.async.solo_mode = (function cljs$core$async$solo_mode(mix,mode){
return cljs.core.async.solo_mode_STAR_.call(null,mix,mode);
});

cljs.core.async.Pub = (function (){var obj40054 = {};
return obj40054;
})();

cljs.core.async.sub_STAR_ = (function cljs$core$async$sub_STAR_(p,v,ch,close_QMARK_){
if((function (){var and__16057__auto__ = p;
if(and__16057__auto__){
return p.cljs$core$async$Pub$sub_STAR_$arity$4;
} else {
return and__16057__auto__;
}
})()){
return p.cljs$core$async$Pub$sub_STAR_$arity$4(p,v,ch,close_QMARK_);
} else {
var x__16705__auto__ = (((p == null))?null:p);
return (function (){var or__16069__auto__ = (cljs.core.async.sub_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.sub_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.sub*",p);
}
}
})().call(null,p,v,ch,close_QMARK_);
}
});

cljs.core.async.unsub_STAR_ = (function cljs$core$async$unsub_STAR_(p,v,ch){
if((function (){var and__16057__auto__ = p;
if(and__16057__auto__){
return p.cljs$core$async$Pub$unsub_STAR_$arity$3;
} else {
return and__16057__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_STAR_$arity$3(p,v,ch);
} else {
var x__16705__auto__ = (((p == null))?null:p);
return (function (){var or__16069__auto__ = (cljs.core.async.unsub_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.unsub_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub*",p);
}
}
})().call(null,p,v,ch);
}
});

cljs.core.async.unsub_all_STAR_ = (function cljs$core$async$unsub_all_STAR_(){
var G__40056 = arguments.length;
switch (G__40056) {
case 1:
return cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$1 = (function (p){
if((function (){var and__16057__auto__ = p;
if(and__16057__auto__){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$1;
} else {
return and__16057__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$1(p);
} else {
var x__16705__auto__ = (((p == null))?null:p);
return (function (){var or__16069__auto__ = (cljs.core.async.unsub_all_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.unsub_all_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub-all*",p);
}
}
})().call(null,p);
}
});

cljs.core.async.unsub_all_STAR_.cljs$core$IFn$_invoke$arity$2 = (function (p,v){
if((function (){var and__16057__auto__ = p;
if(and__16057__auto__){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$2;
} else {
return and__16057__auto__;
}
})()){
return p.cljs$core$async$Pub$unsub_all_STAR_$arity$2(p,v);
} else {
var x__16705__auto__ = (((p == null))?null:p);
return (function (){var or__16069__auto__ = (cljs.core.async.unsub_all_STAR_[goog.typeOf(x__16705__auto__)]);
if(or__16069__auto__){
return or__16069__auto__;
} else {
var or__16069__auto____$1 = (cljs.core.async.unsub_all_STAR_["_"]);
if(or__16069__auto____$1){
return or__16069__auto____$1;
} else {
throw cljs.core.missing_protocol.call(null,"Pub.unsub-all*",p);
}
}
})().call(null,p,v);
}
});

cljs.core.async.unsub_all_STAR_.cljs$lang$maxFixedArity = 2;

/**
 * Creates and returns a pub(lication) of the supplied channel,
 * partitioned into topics by the topic-fn. topic-fn will be applied to
 * each value on the channel and the result will determine the 'topic'
 * on which that value will be put. Channels can be subscribed to
 * receive copies of topics using 'sub', and unsubscribed using
 * 'unsub'. Each topic will be handled by an internal mult on a
 * dedicated channel. By default these internal channels are
 * unbuffered, but a buf-fn can be supplied which, given a topic,
 * creates a buffer with desired properties.
 * 
 * Each item is distributed to all subs in parallel and synchronously,
 * i.e. each sub must accept before the next item is distributed. Use
 * buffering/windowing to prevent slow subs from holding up the pub.
 * 
 * Items received when there are no matching subs get dropped.
 * 
 * Note that if buf-fns are used then each topic is handled
 * asynchronously, i.e. if a channel is subscribed to more than one
 * topic it should not expect them to be interleaved identically with
 * the source.
 */
cljs.core.async.pub = (function cljs$core$async$pub(){
var G__40060 = arguments.length;
switch (G__40060) {
case 2:
return cljs.core.async.pub.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.pub.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.pub.cljs$core$IFn$_invoke$arity$2 = (function (ch,topic_fn){
return cljs.core.async.pub.call(null,ch,topic_fn,cljs.core.constantly.call(null,null));
});

cljs.core.async.pub.cljs$core$IFn$_invoke$arity$3 = (function (ch,topic_fn,buf_fn){
var mults = cljs.core.atom.call(null,cljs.core.PersistentArrayMap.EMPTY);
var ensure_mult = ((function (mults){
return (function (topic){
var or__16069__auto__ = cljs.core.get.call(null,cljs.core.deref.call(null,mults),topic);
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return cljs.core.get.call(null,cljs.core.swap_BANG_.call(null,mults,((function (or__16069__auto__,mults){
return (function (p1__40058_SHARP_){
if(cljs.core.truth_(p1__40058_SHARP_.call(null,topic))){
return p1__40058_SHARP_;
} else {
return cljs.core.assoc.call(null,p1__40058_SHARP_,topic,cljs.core.async.mult.call(null,cljs.core.async.chan.call(null,buf_fn.call(null,topic))));
}
});})(or__16069__auto__,mults))
),topic);
}
});})(mults))
;
var p = (function (){
if(typeof cljs.core.async.t40061 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t40061 = (function (ch,topic_fn,buf_fn,mults,ensure_mult,meta40062){
this.ch = ch;
this.topic_fn = topic_fn;
this.buf_fn = buf_fn;
this.mults = mults;
this.ensure_mult = ensure_mult;
this.meta40062 = meta40062;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t40061.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (mults,ensure_mult){
return (function (_40063,meta40062__$1){
var self__ = this;
var _40063__$1 = this;
return (new cljs.core.async.t40061(self__.ch,self__.topic_fn,self__.buf_fn,self__.mults,self__.ensure_mult,meta40062__$1));
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (mults,ensure_mult){
return (function (_40063){
var self__ = this;
var _40063__$1 = this;
return self__.meta40062;
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$async$Mux$ = true;

cljs.core.async.t40061.prototype.cljs$core$async$Mux$muxch_STAR_$arity$1 = ((function (mults,ensure_mult){
return (function (_){
var self__ = this;
var ___$1 = this;
return self__.ch;
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$async$Pub$ = true;

cljs.core.async.t40061.prototype.cljs$core$async$Pub$sub_STAR_$arity$4 = ((function (mults,ensure_mult){
return (function (p,topic,ch__$1,close_QMARK_){
var self__ = this;
var p__$1 = this;
var m = self__.ensure_mult.call(null,topic);
return cljs.core.async.tap.call(null,m,ch__$1,close_QMARK_);
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$async$Pub$unsub_STAR_$arity$3 = ((function (mults,ensure_mult){
return (function (p,topic,ch__$1){
var self__ = this;
var p__$1 = this;
var temp__4425__auto__ = cljs.core.get.call(null,cljs.core.deref.call(null,self__.mults),topic);
if(cljs.core.truth_(temp__4425__auto__)){
var m = temp__4425__auto__;
return cljs.core.async.untap.call(null,m,ch__$1);
} else {
return null;
}
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$async$Pub$unsub_all_STAR_$arity$1 = ((function (mults,ensure_mult){
return (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.reset_BANG_.call(null,self__.mults,cljs.core.PersistentArrayMap.EMPTY);
});})(mults,ensure_mult))
;

cljs.core.async.t40061.prototype.cljs$core$async$Pub$unsub_all_STAR_$arity$2 = ((function (mults,ensure_mult){
return (function (_,topic){
var self__ = this;
var ___$1 = this;
return cljs.core.swap_BANG_.call(null,self__.mults,cljs.core.dissoc,topic);
});})(mults,ensure_mult))
;

cljs.core.async.t40061.getBasis = ((function (mults,ensure_mult){
return (function (){
return new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"topic-fn","topic-fn",-862449736,null),new cljs.core.Symbol(null,"buf-fn","buf-fn",-1200281591,null),new cljs.core.Symbol(null,"mults","mults",-461114485,null),new cljs.core.Symbol(null,"ensure-mult","ensure-mult",1796584816,null),new cljs.core.Symbol(null,"meta40062","meta40062",-956334256,null)], null);
});})(mults,ensure_mult))
;

cljs.core.async.t40061.cljs$lang$type = true;

cljs.core.async.t40061.cljs$lang$ctorStr = "cljs.core.async/t40061";

cljs.core.async.t40061.cljs$lang$ctorPrWriter = ((function (mults,ensure_mult){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t40061");
});})(mults,ensure_mult))
;

cljs.core.async.__GT_t40061 = ((function (mults,ensure_mult){
return (function cljs$core$async$__GT_t40061(ch__$1,topic_fn__$1,buf_fn__$1,mults__$1,ensure_mult__$1,meta40062){
return (new cljs.core.async.t40061(ch__$1,topic_fn__$1,buf_fn__$1,mults__$1,ensure_mult__$1,meta40062));
});})(mults,ensure_mult))
;

}

return (new cljs.core.async.t40061(ch,topic_fn,buf_fn,mults,ensure_mult,cljs.core.PersistentArrayMap.EMPTY));
})()
;
var c__23633__auto___40184 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40184,mults,ensure_mult,p){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40184,mults,ensure_mult,p){
return (function (state_40135){
var state_val_40136 = (state_40135[(1)]);
if((state_val_40136 === (7))){
var inst_40131 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40137_40185 = state_40135__$1;
(statearr_40137_40185[(2)] = inst_40131);

(statearr_40137_40185[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (20))){
var state_40135__$1 = state_40135;
var statearr_40138_40186 = state_40135__$1;
(statearr_40138_40186[(2)] = null);

(statearr_40138_40186[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (1))){
var state_40135__$1 = state_40135;
var statearr_40139_40187 = state_40135__$1;
(statearr_40139_40187[(2)] = null);

(statearr_40139_40187[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (24))){
var inst_40114 = (state_40135[(7)]);
var inst_40123 = cljs.core.swap_BANG_.call(null,mults,cljs.core.dissoc,inst_40114);
var state_40135__$1 = state_40135;
var statearr_40140_40188 = state_40135__$1;
(statearr_40140_40188[(2)] = inst_40123);

(statearr_40140_40188[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (4))){
var inst_40066 = (state_40135[(8)]);
var inst_40066__$1 = (state_40135[(2)]);
var inst_40067 = (inst_40066__$1 == null);
var state_40135__$1 = (function (){var statearr_40141 = state_40135;
(statearr_40141[(8)] = inst_40066__$1);

return statearr_40141;
})();
if(cljs.core.truth_(inst_40067)){
var statearr_40142_40189 = state_40135__$1;
(statearr_40142_40189[(1)] = (5));

} else {
var statearr_40143_40190 = state_40135__$1;
(statearr_40143_40190[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (15))){
var inst_40108 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40144_40191 = state_40135__$1;
(statearr_40144_40191[(2)] = inst_40108);

(statearr_40144_40191[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (21))){
var inst_40128 = (state_40135[(2)]);
var state_40135__$1 = (function (){var statearr_40145 = state_40135;
(statearr_40145[(9)] = inst_40128);

return statearr_40145;
})();
var statearr_40146_40192 = state_40135__$1;
(statearr_40146_40192[(2)] = null);

(statearr_40146_40192[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (13))){
var inst_40090 = (state_40135[(10)]);
var inst_40092 = cljs.core.chunked_seq_QMARK_.call(null,inst_40090);
var state_40135__$1 = state_40135;
if(inst_40092){
var statearr_40147_40193 = state_40135__$1;
(statearr_40147_40193[(1)] = (16));

} else {
var statearr_40148_40194 = state_40135__$1;
(statearr_40148_40194[(1)] = (17));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (22))){
var inst_40120 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
if(cljs.core.truth_(inst_40120)){
var statearr_40149_40195 = state_40135__$1;
(statearr_40149_40195[(1)] = (23));

} else {
var statearr_40150_40196 = state_40135__$1;
(statearr_40150_40196[(1)] = (24));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (6))){
var inst_40114 = (state_40135[(7)]);
var inst_40116 = (state_40135[(11)]);
var inst_40066 = (state_40135[(8)]);
var inst_40114__$1 = topic_fn.call(null,inst_40066);
var inst_40115 = cljs.core.deref.call(null,mults);
var inst_40116__$1 = cljs.core.get.call(null,inst_40115,inst_40114__$1);
var state_40135__$1 = (function (){var statearr_40151 = state_40135;
(statearr_40151[(7)] = inst_40114__$1);

(statearr_40151[(11)] = inst_40116__$1);

return statearr_40151;
})();
if(cljs.core.truth_(inst_40116__$1)){
var statearr_40152_40197 = state_40135__$1;
(statearr_40152_40197[(1)] = (19));

} else {
var statearr_40153_40198 = state_40135__$1;
(statearr_40153_40198[(1)] = (20));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (25))){
var inst_40125 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40154_40199 = state_40135__$1;
(statearr_40154_40199[(2)] = inst_40125);

(statearr_40154_40199[(1)] = (21));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (17))){
var inst_40090 = (state_40135[(10)]);
var inst_40099 = cljs.core.first.call(null,inst_40090);
var inst_40100 = cljs.core.async.muxch_STAR_.call(null,inst_40099);
var inst_40101 = cljs.core.async.close_BANG_.call(null,inst_40100);
var inst_40102 = cljs.core.next.call(null,inst_40090);
var inst_40076 = inst_40102;
var inst_40077 = null;
var inst_40078 = (0);
var inst_40079 = (0);
var state_40135__$1 = (function (){var statearr_40155 = state_40135;
(statearr_40155[(12)] = inst_40076);

(statearr_40155[(13)] = inst_40078);

(statearr_40155[(14)] = inst_40101);

(statearr_40155[(15)] = inst_40077);

(statearr_40155[(16)] = inst_40079);

return statearr_40155;
})();
var statearr_40156_40200 = state_40135__$1;
(statearr_40156_40200[(2)] = null);

(statearr_40156_40200[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (3))){
var inst_40133 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40135__$1,inst_40133);
} else {
if((state_val_40136 === (12))){
var inst_40110 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40157_40201 = state_40135__$1;
(statearr_40157_40201[(2)] = inst_40110);

(statearr_40157_40201[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (2))){
var state_40135__$1 = state_40135;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40135__$1,(4),ch);
} else {
if((state_val_40136 === (23))){
var state_40135__$1 = state_40135;
var statearr_40158_40202 = state_40135__$1;
(statearr_40158_40202[(2)] = null);

(statearr_40158_40202[(1)] = (25));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (19))){
var inst_40116 = (state_40135[(11)]);
var inst_40066 = (state_40135[(8)]);
var inst_40118 = cljs.core.async.muxch_STAR_.call(null,inst_40116);
var state_40135__$1 = state_40135;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40135__$1,(22),inst_40118,inst_40066);
} else {
if((state_val_40136 === (11))){
var inst_40076 = (state_40135[(12)]);
var inst_40090 = (state_40135[(10)]);
var inst_40090__$1 = cljs.core.seq.call(null,inst_40076);
var state_40135__$1 = (function (){var statearr_40159 = state_40135;
(statearr_40159[(10)] = inst_40090__$1);

return statearr_40159;
})();
if(inst_40090__$1){
var statearr_40160_40203 = state_40135__$1;
(statearr_40160_40203[(1)] = (13));

} else {
var statearr_40161_40204 = state_40135__$1;
(statearr_40161_40204[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (9))){
var inst_40112 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40162_40205 = state_40135__$1;
(statearr_40162_40205[(2)] = inst_40112);

(statearr_40162_40205[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (5))){
var inst_40073 = cljs.core.deref.call(null,mults);
var inst_40074 = cljs.core.vals.call(null,inst_40073);
var inst_40075 = cljs.core.seq.call(null,inst_40074);
var inst_40076 = inst_40075;
var inst_40077 = null;
var inst_40078 = (0);
var inst_40079 = (0);
var state_40135__$1 = (function (){var statearr_40163 = state_40135;
(statearr_40163[(12)] = inst_40076);

(statearr_40163[(13)] = inst_40078);

(statearr_40163[(15)] = inst_40077);

(statearr_40163[(16)] = inst_40079);

return statearr_40163;
})();
var statearr_40164_40206 = state_40135__$1;
(statearr_40164_40206[(2)] = null);

(statearr_40164_40206[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (14))){
var state_40135__$1 = state_40135;
var statearr_40168_40207 = state_40135__$1;
(statearr_40168_40207[(2)] = null);

(statearr_40168_40207[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (16))){
var inst_40090 = (state_40135[(10)]);
var inst_40094 = cljs.core.chunk_first.call(null,inst_40090);
var inst_40095 = cljs.core.chunk_rest.call(null,inst_40090);
var inst_40096 = cljs.core.count.call(null,inst_40094);
var inst_40076 = inst_40095;
var inst_40077 = inst_40094;
var inst_40078 = inst_40096;
var inst_40079 = (0);
var state_40135__$1 = (function (){var statearr_40169 = state_40135;
(statearr_40169[(12)] = inst_40076);

(statearr_40169[(13)] = inst_40078);

(statearr_40169[(15)] = inst_40077);

(statearr_40169[(16)] = inst_40079);

return statearr_40169;
})();
var statearr_40170_40208 = state_40135__$1;
(statearr_40170_40208[(2)] = null);

(statearr_40170_40208[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (10))){
var inst_40076 = (state_40135[(12)]);
var inst_40078 = (state_40135[(13)]);
var inst_40077 = (state_40135[(15)]);
var inst_40079 = (state_40135[(16)]);
var inst_40084 = cljs.core._nth.call(null,inst_40077,inst_40079);
var inst_40085 = cljs.core.async.muxch_STAR_.call(null,inst_40084);
var inst_40086 = cljs.core.async.close_BANG_.call(null,inst_40085);
var inst_40087 = (inst_40079 + (1));
var tmp40165 = inst_40076;
var tmp40166 = inst_40078;
var tmp40167 = inst_40077;
var inst_40076__$1 = tmp40165;
var inst_40077__$1 = tmp40167;
var inst_40078__$1 = tmp40166;
var inst_40079__$1 = inst_40087;
var state_40135__$1 = (function (){var statearr_40171 = state_40135;
(statearr_40171[(12)] = inst_40076__$1);

(statearr_40171[(13)] = inst_40078__$1);

(statearr_40171[(15)] = inst_40077__$1);

(statearr_40171[(17)] = inst_40086);

(statearr_40171[(16)] = inst_40079__$1);

return statearr_40171;
})();
var statearr_40172_40209 = state_40135__$1;
(statearr_40172_40209[(2)] = null);

(statearr_40172_40209[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (18))){
var inst_40105 = (state_40135[(2)]);
var state_40135__$1 = state_40135;
var statearr_40173_40210 = state_40135__$1;
(statearr_40173_40210[(2)] = inst_40105);

(statearr_40173_40210[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40136 === (8))){
var inst_40078 = (state_40135[(13)]);
var inst_40079 = (state_40135[(16)]);
var inst_40081 = (inst_40079 < inst_40078);
var inst_40082 = inst_40081;
var state_40135__$1 = state_40135;
if(cljs.core.truth_(inst_40082)){
var statearr_40174_40211 = state_40135__$1;
(statearr_40174_40211[(1)] = (10));

} else {
var statearr_40175_40212 = state_40135__$1;
(statearr_40175_40212[(1)] = (11));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40184,mults,ensure_mult,p))
;
return ((function (switch__23571__auto__,c__23633__auto___40184,mults,ensure_mult,p){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40179 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40179[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40179[(1)] = (1));

return statearr_40179;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40135){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40135);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40180){if((e40180 instanceof Object)){
var ex__23575__auto__ = e40180;
var statearr_40181_40213 = state_40135;
(statearr_40181_40213[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40135);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40180;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40214 = state_40135;
state_40135 = G__40214;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40135){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40135);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40184,mults,ensure_mult,p))
})();
var state__23635__auto__ = (function (){var statearr_40182 = f__23634__auto__.call(null);
(statearr_40182[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40184);

return statearr_40182;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40184,mults,ensure_mult,p))
);


return p;
});

cljs.core.async.pub.cljs$lang$maxFixedArity = 3;
/**
 * Subscribes a channel to a topic of a pub.
 * 
 * By default the channel will be closed when the source closes,
 * but can be determined by the close? parameter.
 */
cljs.core.async.sub = (function cljs$core$async$sub(){
var G__40216 = arguments.length;
switch (G__40216) {
case 3:
return cljs.core.async.sub.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 4:
return cljs.core.async.sub.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.sub.cljs$core$IFn$_invoke$arity$3 = (function (p,topic,ch){
return cljs.core.async.sub.call(null,p,topic,ch,true);
});

cljs.core.async.sub.cljs$core$IFn$_invoke$arity$4 = (function (p,topic,ch,close_QMARK_){
return cljs.core.async.sub_STAR_.call(null,p,topic,ch,close_QMARK_);
});

cljs.core.async.sub.cljs$lang$maxFixedArity = 4;
/**
 * Unsubscribes a channel from a topic of a pub
 */
cljs.core.async.unsub = (function cljs$core$async$unsub(p,topic,ch){
return cljs.core.async.unsub_STAR_.call(null,p,topic,ch);
});
/**
 * Unsubscribes all channels from a pub, or a topic of a pub
 */
cljs.core.async.unsub_all = (function cljs$core$async$unsub_all(){
var G__40219 = arguments.length;
switch (G__40219) {
case 1:
return cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$1 = (function (p){
return cljs.core.async.unsub_all_STAR_.call(null,p);
});

cljs.core.async.unsub_all.cljs$core$IFn$_invoke$arity$2 = (function (p,topic){
return cljs.core.async.unsub_all_STAR_.call(null,p,topic);
});

cljs.core.async.unsub_all.cljs$lang$maxFixedArity = 2;
/**
 * Takes a function and a collection of source channels, and returns a
 * channel which contains the values produced by applying f to the set
 * of first items taken from each source channel, followed by applying
 * f to the set of second items from each channel, until any one of the
 * channels is closed, at which point the output channel will be
 * closed. The returned channel will be unbuffered by default, or a
 * buf-or-n can be supplied
 */
cljs.core.async.map = (function cljs$core$async$map(){
var G__40222 = arguments.length;
switch (G__40222) {
case 2:
return cljs.core.async.map.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.map.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.map.cljs$core$IFn$_invoke$arity$2 = (function (f,chs){
return cljs.core.async.map.call(null,f,chs,null);
});

cljs.core.async.map.cljs$core$IFn$_invoke$arity$3 = (function (f,chs,buf_or_n){
var chs__$1 = cljs.core.vec.call(null,chs);
var out = cljs.core.async.chan.call(null,buf_or_n);
var cnt = cljs.core.count.call(null,chs__$1);
var rets = cljs.core.object_array.call(null,cnt);
var dchan = cljs.core.async.chan.call(null,(1));
var dctr = cljs.core.atom.call(null,null);
var done = cljs.core.mapv.call(null,((function (chs__$1,out,cnt,rets,dchan,dctr){
return (function (i){
return ((function (chs__$1,out,cnt,rets,dchan,dctr){
return (function (ret){
(rets[i] = ret);

if((cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec) === (0))){
return cljs.core.async.put_BANG_.call(null,dchan,rets.slice((0)));
} else {
return null;
}
});
;})(chs__$1,out,cnt,rets,dchan,dctr))
});})(chs__$1,out,cnt,rets,dchan,dctr))
,cljs.core.range.call(null,cnt));
var c__23633__auto___40292 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function (state_40261){
var state_val_40262 = (state_40261[(1)]);
if((state_val_40262 === (7))){
var state_40261__$1 = state_40261;
var statearr_40263_40293 = state_40261__$1;
(statearr_40263_40293[(2)] = null);

(statearr_40263_40293[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (1))){
var state_40261__$1 = state_40261;
var statearr_40264_40294 = state_40261__$1;
(statearr_40264_40294[(2)] = null);

(statearr_40264_40294[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (4))){
var inst_40225 = (state_40261[(7)]);
var inst_40227 = (inst_40225 < cnt);
var state_40261__$1 = state_40261;
if(cljs.core.truth_(inst_40227)){
var statearr_40265_40295 = state_40261__$1;
(statearr_40265_40295[(1)] = (6));

} else {
var statearr_40266_40296 = state_40261__$1;
(statearr_40266_40296[(1)] = (7));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (15))){
var inst_40257 = (state_40261[(2)]);
var state_40261__$1 = state_40261;
var statearr_40267_40297 = state_40261__$1;
(statearr_40267_40297[(2)] = inst_40257);

(statearr_40267_40297[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (13))){
var inst_40250 = cljs.core.async.close_BANG_.call(null,out);
var state_40261__$1 = state_40261;
var statearr_40268_40298 = state_40261__$1;
(statearr_40268_40298[(2)] = inst_40250);

(statearr_40268_40298[(1)] = (15));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (6))){
var state_40261__$1 = state_40261;
var statearr_40269_40299 = state_40261__$1;
(statearr_40269_40299[(2)] = null);

(statearr_40269_40299[(1)] = (11));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (3))){
var inst_40259 = (state_40261[(2)]);
var state_40261__$1 = state_40261;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40261__$1,inst_40259);
} else {
if((state_val_40262 === (12))){
var inst_40247 = (state_40261[(8)]);
var inst_40247__$1 = (state_40261[(2)]);
var inst_40248 = cljs.core.some.call(null,cljs.core.nil_QMARK_,inst_40247__$1);
var state_40261__$1 = (function (){var statearr_40270 = state_40261;
(statearr_40270[(8)] = inst_40247__$1);

return statearr_40270;
})();
if(cljs.core.truth_(inst_40248)){
var statearr_40271_40300 = state_40261__$1;
(statearr_40271_40300[(1)] = (13));

} else {
var statearr_40272_40301 = state_40261__$1;
(statearr_40272_40301[(1)] = (14));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (2))){
var inst_40224 = cljs.core.reset_BANG_.call(null,dctr,cnt);
var inst_40225 = (0);
var state_40261__$1 = (function (){var statearr_40273 = state_40261;
(statearr_40273[(9)] = inst_40224);

(statearr_40273[(7)] = inst_40225);

return statearr_40273;
})();
var statearr_40274_40302 = state_40261__$1;
(statearr_40274_40302[(2)] = null);

(statearr_40274_40302[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (11))){
var inst_40225 = (state_40261[(7)]);
var _ = cljs.core.async.impl.ioc_helpers.add_exception_frame.call(null,state_40261,(10),Object,null,(9));
var inst_40234 = chs__$1.call(null,inst_40225);
var inst_40235 = done.call(null,inst_40225);
var inst_40236 = cljs.core.async.take_BANG_.call(null,inst_40234,inst_40235);
var state_40261__$1 = state_40261;
var statearr_40275_40303 = state_40261__$1;
(statearr_40275_40303[(2)] = inst_40236);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40261__$1);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (9))){
var inst_40225 = (state_40261[(7)]);
var inst_40238 = (state_40261[(2)]);
var inst_40239 = (inst_40225 + (1));
var inst_40225__$1 = inst_40239;
var state_40261__$1 = (function (){var statearr_40276 = state_40261;
(statearr_40276[(10)] = inst_40238);

(statearr_40276[(7)] = inst_40225__$1);

return statearr_40276;
})();
var statearr_40277_40304 = state_40261__$1;
(statearr_40277_40304[(2)] = null);

(statearr_40277_40304[(1)] = (4));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (5))){
var inst_40245 = (state_40261[(2)]);
var state_40261__$1 = (function (){var statearr_40278 = state_40261;
(statearr_40278[(11)] = inst_40245);

return statearr_40278;
})();
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40261__$1,(12),dchan);
} else {
if((state_val_40262 === (14))){
var inst_40247 = (state_40261[(8)]);
var inst_40252 = cljs.core.apply.call(null,f,inst_40247);
var state_40261__$1 = state_40261;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40261__$1,(16),out,inst_40252);
} else {
if((state_val_40262 === (16))){
var inst_40254 = (state_40261[(2)]);
var state_40261__$1 = (function (){var statearr_40279 = state_40261;
(statearr_40279[(12)] = inst_40254);

return statearr_40279;
})();
var statearr_40280_40305 = state_40261__$1;
(statearr_40280_40305[(2)] = null);

(statearr_40280_40305[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (10))){
var inst_40229 = (state_40261[(2)]);
var inst_40230 = cljs.core.swap_BANG_.call(null,dctr,cljs.core.dec);
var state_40261__$1 = (function (){var statearr_40281 = state_40261;
(statearr_40281[(13)] = inst_40229);

return statearr_40281;
})();
var statearr_40282_40306 = state_40261__$1;
(statearr_40282_40306[(2)] = inst_40230);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40261__$1);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40262 === (8))){
var inst_40243 = (state_40261[(2)]);
var state_40261__$1 = state_40261;
var statearr_40283_40307 = state_40261__$1;
(statearr_40283_40307[(2)] = inst_40243);

(statearr_40283_40307[(1)] = (5));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done))
;
return ((function (switch__23571__auto__,c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40287 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40287[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40287[(1)] = (1));

return statearr_40287;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40261){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40261);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40288){if((e40288 instanceof Object)){
var ex__23575__auto__ = e40288;
var statearr_40289_40308 = state_40261;
(statearr_40289_40308[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40261);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40288;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40309 = state_40261;
state_40261 = G__40309;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40261){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40261);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done))
})();
var state__23635__auto__ = (function (){var statearr_40290 = f__23634__auto__.call(null);
(statearr_40290[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40292);

return statearr_40290;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40292,chs__$1,out,cnt,rets,dchan,dctr,done))
);


return out;
});

cljs.core.async.map.cljs$lang$maxFixedArity = 3;
/**
 * Takes a collection of source channels and returns a channel which
 * contains all values taken from them. The returned channel will be
 * unbuffered by default, or a buf-or-n can be supplied. The channel
 * will close after all the source channels have closed.
 */
cljs.core.async.merge = (function cljs$core$async$merge(){
var G__40312 = arguments.length;
switch (G__40312) {
case 1:
return cljs.core.async.merge.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.merge.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.merge.cljs$core$IFn$_invoke$arity$1 = (function (chs){
return cljs.core.async.merge.call(null,chs,null);
});

cljs.core.async.merge.cljs$core$IFn$_invoke$arity$2 = (function (chs,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40367 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40367,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40367,out){
return (function (state_40342){
var state_val_40343 = (state_40342[(1)]);
if((state_val_40343 === (7))){
var inst_40321 = (state_40342[(7)]);
var inst_40322 = (state_40342[(8)]);
var inst_40321__$1 = (state_40342[(2)]);
var inst_40322__$1 = cljs.core.nth.call(null,inst_40321__$1,(0),null);
var inst_40323 = cljs.core.nth.call(null,inst_40321__$1,(1),null);
var inst_40324 = (inst_40322__$1 == null);
var state_40342__$1 = (function (){var statearr_40344 = state_40342;
(statearr_40344[(9)] = inst_40323);

(statearr_40344[(7)] = inst_40321__$1);

(statearr_40344[(8)] = inst_40322__$1);

return statearr_40344;
})();
if(cljs.core.truth_(inst_40324)){
var statearr_40345_40368 = state_40342__$1;
(statearr_40345_40368[(1)] = (8));

} else {
var statearr_40346_40369 = state_40342__$1;
(statearr_40346_40369[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (1))){
var inst_40313 = cljs.core.vec.call(null,chs);
var inst_40314 = inst_40313;
var state_40342__$1 = (function (){var statearr_40347 = state_40342;
(statearr_40347[(10)] = inst_40314);

return statearr_40347;
})();
var statearr_40348_40370 = state_40342__$1;
(statearr_40348_40370[(2)] = null);

(statearr_40348_40370[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (4))){
var inst_40314 = (state_40342[(10)]);
var state_40342__$1 = state_40342;
return cljs.core.async.ioc_alts_BANG_.call(null,state_40342__$1,(7),inst_40314);
} else {
if((state_val_40343 === (6))){
var inst_40338 = (state_40342[(2)]);
var state_40342__$1 = state_40342;
var statearr_40349_40371 = state_40342__$1;
(statearr_40349_40371[(2)] = inst_40338);

(statearr_40349_40371[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (3))){
var inst_40340 = (state_40342[(2)]);
var state_40342__$1 = state_40342;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40342__$1,inst_40340);
} else {
if((state_val_40343 === (2))){
var inst_40314 = (state_40342[(10)]);
var inst_40316 = cljs.core.count.call(null,inst_40314);
var inst_40317 = (inst_40316 > (0));
var state_40342__$1 = state_40342;
if(cljs.core.truth_(inst_40317)){
var statearr_40351_40372 = state_40342__$1;
(statearr_40351_40372[(1)] = (4));

} else {
var statearr_40352_40373 = state_40342__$1;
(statearr_40352_40373[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (11))){
var inst_40314 = (state_40342[(10)]);
var inst_40331 = (state_40342[(2)]);
var tmp40350 = inst_40314;
var inst_40314__$1 = tmp40350;
var state_40342__$1 = (function (){var statearr_40353 = state_40342;
(statearr_40353[(11)] = inst_40331);

(statearr_40353[(10)] = inst_40314__$1);

return statearr_40353;
})();
var statearr_40354_40374 = state_40342__$1;
(statearr_40354_40374[(2)] = null);

(statearr_40354_40374[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (9))){
var inst_40322 = (state_40342[(8)]);
var state_40342__$1 = state_40342;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40342__$1,(11),out,inst_40322);
} else {
if((state_val_40343 === (5))){
var inst_40336 = cljs.core.async.close_BANG_.call(null,out);
var state_40342__$1 = state_40342;
var statearr_40355_40375 = state_40342__$1;
(statearr_40355_40375[(2)] = inst_40336);

(statearr_40355_40375[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (10))){
var inst_40334 = (state_40342[(2)]);
var state_40342__$1 = state_40342;
var statearr_40356_40376 = state_40342__$1;
(statearr_40356_40376[(2)] = inst_40334);

(statearr_40356_40376[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40343 === (8))){
var inst_40323 = (state_40342[(9)]);
var inst_40321 = (state_40342[(7)]);
var inst_40314 = (state_40342[(10)]);
var inst_40322 = (state_40342[(8)]);
var inst_40326 = (function (){var cs = inst_40314;
var vec__40319 = inst_40321;
var v = inst_40322;
var c = inst_40323;
return ((function (cs,vec__40319,v,c,inst_40323,inst_40321,inst_40314,inst_40322,state_val_40343,c__23633__auto___40367,out){
return (function (p1__40310_SHARP_){
return cljs.core.not_EQ_.call(null,c,p1__40310_SHARP_);
});
;})(cs,vec__40319,v,c,inst_40323,inst_40321,inst_40314,inst_40322,state_val_40343,c__23633__auto___40367,out))
})();
var inst_40327 = cljs.core.filterv.call(null,inst_40326,inst_40314);
var inst_40314__$1 = inst_40327;
var state_40342__$1 = (function (){var statearr_40357 = state_40342;
(statearr_40357[(10)] = inst_40314__$1);

return statearr_40357;
})();
var statearr_40358_40377 = state_40342__$1;
(statearr_40358_40377[(2)] = null);

(statearr_40358_40377[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40367,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40367,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40362 = [null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40362[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40362[(1)] = (1));

return statearr_40362;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40342){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40342);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40363){if((e40363 instanceof Object)){
var ex__23575__auto__ = e40363;
var statearr_40364_40378 = state_40342;
(statearr_40364_40378[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40342);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40363;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40379 = state_40342;
state_40342 = G__40379;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40342){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40342);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40367,out))
})();
var state__23635__auto__ = (function (){var statearr_40365 = f__23634__auto__.call(null);
(statearr_40365[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40367);

return statearr_40365;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40367,out))
);


return out;
});

cljs.core.async.merge.cljs$lang$maxFixedArity = 2;
/**
 * Returns a channel containing the single (collection) result of the
 * items taken from the channel conjoined to the supplied
 * collection. ch must close before into produces a result.
 */
cljs.core.async.into = (function cljs$core$async$into(coll,ch){
return cljs.core.async.reduce.call(null,cljs.core.conj,coll,ch);
});
/**
 * Returns a channel that will return, at most, n items from ch. After n items
 * have been returned, or ch has been closed, the return chanel will close.
 * 
 * The output channel is unbuffered by default, unless buf-or-n is given.
 */
cljs.core.async.take = (function cljs$core$async$take(){
var G__40381 = arguments.length;
switch (G__40381) {
case 2:
return cljs.core.async.take.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.take.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.take.cljs$core$IFn$_invoke$arity$2 = (function (n,ch){
return cljs.core.async.take.call(null,n,ch,null);
});

cljs.core.async.take.cljs$core$IFn$_invoke$arity$3 = (function (n,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40429 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40429,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40429,out){
return (function (state_40405){
var state_val_40406 = (state_40405[(1)]);
if((state_val_40406 === (7))){
var inst_40387 = (state_40405[(7)]);
var inst_40387__$1 = (state_40405[(2)]);
var inst_40388 = (inst_40387__$1 == null);
var inst_40389 = cljs.core.not.call(null,inst_40388);
var state_40405__$1 = (function (){var statearr_40407 = state_40405;
(statearr_40407[(7)] = inst_40387__$1);

return statearr_40407;
})();
if(inst_40389){
var statearr_40408_40430 = state_40405__$1;
(statearr_40408_40430[(1)] = (8));

} else {
var statearr_40409_40431 = state_40405__$1;
(statearr_40409_40431[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (1))){
var inst_40382 = (0);
var state_40405__$1 = (function (){var statearr_40410 = state_40405;
(statearr_40410[(8)] = inst_40382);

return statearr_40410;
})();
var statearr_40411_40432 = state_40405__$1;
(statearr_40411_40432[(2)] = null);

(statearr_40411_40432[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (4))){
var state_40405__$1 = state_40405;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40405__$1,(7),ch);
} else {
if((state_val_40406 === (6))){
var inst_40400 = (state_40405[(2)]);
var state_40405__$1 = state_40405;
var statearr_40412_40433 = state_40405__$1;
(statearr_40412_40433[(2)] = inst_40400);

(statearr_40412_40433[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (3))){
var inst_40402 = (state_40405[(2)]);
var inst_40403 = cljs.core.async.close_BANG_.call(null,out);
var state_40405__$1 = (function (){var statearr_40413 = state_40405;
(statearr_40413[(9)] = inst_40402);

return statearr_40413;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40405__$1,inst_40403);
} else {
if((state_val_40406 === (2))){
var inst_40382 = (state_40405[(8)]);
var inst_40384 = (inst_40382 < n);
var state_40405__$1 = state_40405;
if(cljs.core.truth_(inst_40384)){
var statearr_40414_40434 = state_40405__$1;
(statearr_40414_40434[(1)] = (4));

} else {
var statearr_40415_40435 = state_40405__$1;
(statearr_40415_40435[(1)] = (5));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (11))){
var inst_40382 = (state_40405[(8)]);
var inst_40392 = (state_40405[(2)]);
var inst_40393 = (inst_40382 + (1));
var inst_40382__$1 = inst_40393;
var state_40405__$1 = (function (){var statearr_40416 = state_40405;
(statearr_40416[(10)] = inst_40392);

(statearr_40416[(8)] = inst_40382__$1);

return statearr_40416;
})();
var statearr_40417_40436 = state_40405__$1;
(statearr_40417_40436[(2)] = null);

(statearr_40417_40436[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (9))){
var state_40405__$1 = state_40405;
var statearr_40418_40437 = state_40405__$1;
(statearr_40418_40437[(2)] = null);

(statearr_40418_40437[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (5))){
var state_40405__$1 = state_40405;
var statearr_40419_40438 = state_40405__$1;
(statearr_40419_40438[(2)] = null);

(statearr_40419_40438[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (10))){
var inst_40397 = (state_40405[(2)]);
var state_40405__$1 = state_40405;
var statearr_40420_40439 = state_40405__$1;
(statearr_40420_40439[(2)] = inst_40397);

(statearr_40420_40439[(1)] = (6));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40406 === (8))){
var inst_40387 = (state_40405[(7)]);
var state_40405__$1 = state_40405;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40405__$1,(11),out,inst_40387);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40429,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40429,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40424 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_40424[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40424[(1)] = (1));

return statearr_40424;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40405){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40405);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40425){if((e40425 instanceof Object)){
var ex__23575__auto__ = e40425;
var statearr_40426_40440 = state_40405;
(statearr_40426_40440[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40405);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40425;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40441 = state_40405;
state_40405 = G__40441;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40405){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40405);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40429,out))
})();
var state__23635__auto__ = (function (){var statearr_40427 = f__23634__auto__.call(null);
(statearr_40427[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40429);

return statearr_40427;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40429,out))
);


return out;
});

cljs.core.async.take.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.map_LT_ = (function cljs$core$async$map_LT_(f,ch){
if(typeof cljs.core.async.t40449 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t40449 = (function (map_LT_,f,ch,meta40450){
this.map_LT_ = map_LT_;
this.f = f;
this.ch = ch;
this.meta40450 = meta40450;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t40449.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_40451,meta40450__$1){
var self__ = this;
var _40451__$1 = this;
return (new cljs.core.async.t40449(self__.map_LT_,self__.f,self__.ch,meta40450__$1));
});

cljs.core.async.t40449.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_40451){
var self__ = this;
var _40451__$1 = this;
return self__.meta40450;
});

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$Channel$closed_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch);
});

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
var ret = cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,(function (){
if(typeof cljs.core.async.t40452 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t40452 = (function (map_LT_,f,ch,meta40450,_,fn1,meta40453){
this.map_LT_ = map_LT_;
this.f = f;
this.ch = ch;
this.meta40450 = meta40450;
this._ = _;
this.fn1 = fn1;
this.meta40453 = meta40453;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t40452.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = ((function (___$1){
return (function (_40454,meta40453__$1){
var self__ = this;
var _40454__$1 = this;
return (new cljs.core.async.t40452(self__.map_LT_,self__.f,self__.ch,self__.meta40450,self__._,self__.fn1,meta40453__$1));
});})(___$1))
;

cljs.core.async.t40452.prototype.cljs$core$IMeta$_meta$arity$1 = ((function (___$1){
return (function (_40454){
var self__ = this;
var _40454__$1 = this;
return self__.meta40453;
});})(___$1))
;

cljs.core.async.t40452.prototype.cljs$core$async$impl$protocols$Handler$ = true;

cljs.core.async.t40452.prototype.cljs$core$async$impl$protocols$Handler$active_QMARK_$arity$1 = ((function (___$1){
return (function (___$1){
var self__ = this;
var ___$2 = this;
return cljs.core.async.impl.protocols.active_QMARK_.call(null,self__.fn1);
});})(___$1))
;

cljs.core.async.t40452.prototype.cljs$core$async$impl$protocols$Handler$commit$arity$1 = ((function (___$1){
return (function (___$1){
var self__ = this;
var ___$2 = this;
var f1 = cljs.core.async.impl.protocols.commit.call(null,self__.fn1);
return ((function (f1,___$2,___$1){
return (function (p1__40442_SHARP_){
return f1.call(null,(((p1__40442_SHARP_ == null))?null:self__.f.call(null,p1__40442_SHARP_)));
});
;})(f1,___$2,___$1))
});})(___$1))
;

cljs.core.async.t40452.getBasis = ((function (___$1){
return (function (){
return new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map<","map<",-1235808357,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta40450","meta40450",-16292271,null),new cljs.core.Symbol(null,"_","_",-1201019570,null),new cljs.core.Symbol(null,"fn1","fn1",895834444,null),new cljs.core.Symbol(null,"meta40453","meta40453",883498644,null)], null);
});})(___$1))
;

cljs.core.async.t40452.cljs$lang$type = true;

cljs.core.async.t40452.cljs$lang$ctorStr = "cljs.core.async/t40452";

cljs.core.async.t40452.cljs$lang$ctorPrWriter = ((function (___$1){
return (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t40452");
});})(___$1))
;

cljs.core.async.__GT_t40452 = ((function (___$1){
return (function cljs$core$async$map_LT__$___GT_t40452(map_LT___$1,f__$1,ch__$1,meta40450__$1,___$2,fn1__$1,meta40453){
return (new cljs.core.async.t40452(map_LT___$1,f__$1,ch__$1,meta40450__$1,___$2,fn1__$1,meta40453));
});})(___$1))
;

}

return (new cljs.core.async.t40452(self__.map_LT_,self__.f,self__.ch,self__.meta40450,___$1,fn1,cljs.core.PersistentArrayMap.EMPTY));
})()
);
if(cljs.core.truth_((function (){var and__16057__auto__ = ret;
if(cljs.core.truth_(and__16057__auto__)){
return !((cljs.core.deref.call(null,ret) == null));
} else {
return and__16057__auto__;
}
})())){
return cljs.core.async.impl.channels.box.call(null,self__.f.call(null,cljs.core.deref.call(null,ret)));
} else {
return ret;
}
});

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t40449.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,val,fn1);
});

cljs.core.async.t40449.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map<","map<",-1235808357,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta40450","meta40450",-16292271,null)], null);
});

cljs.core.async.t40449.cljs$lang$type = true;

cljs.core.async.t40449.cljs$lang$ctorStr = "cljs.core.async/t40449";

cljs.core.async.t40449.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t40449");
});

cljs.core.async.__GT_t40449 = (function cljs$core$async$map_LT__$___GT_t40449(map_LT___$1,f__$1,ch__$1,meta40450){
return (new cljs.core.async.t40449(map_LT___$1,f__$1,ch__$1,meta40450));
});

}

return (new cljs.core.async.t40449(cljs$core$async$map_LT_,f,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.map_GT_ = (function cljs$core$async$map_GT_(f,ch){
if(typeof cljs.core.async.t40458 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t40458 = (function (map_GT_,f,ch,meta40459){
this.map_GT_ = map_GT_;
this.f = f;
this.ch = ch;
this.meta40459 = meta40459;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t40458.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_40460,meta40459__$1){
var self__ = this;
var _40460__$1 = this;
return (new cljs.core.async.t40458(self__.map_GT_,self__.f,self__.ch,meta40459__$1));
});

cljs.core.async.t40458.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_40460){
var self__ = this;
var _40460__$1 = this;
return self__.meta40459;
});

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,fn1);
});

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t40458.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,self__.f.call(null,val),fn1);
});

cljs.core.async.t40458.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map>","map>",1676369295,null),new cljs.core.Symbol(null,"f","f",43394975,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta40459","meta40459",978010177,null)], null);
});

cljs.core.async.t40458.cljs$lang$type = true;

cljs.core.async.t40458.cljs$lang$ctorStr = "cljs.core.async/t40458";

cljs.core.async.t40458.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t40458");
});

cljs.core.async.__GT_t40458 = (function cljs$core$async$map_GT__$___GT_t40458(map_GT___$1,f__$1,ch__$1,meta40459){
return (new cljs.core.async.t40458(map_GT___$1,f__$1,ch__$1,meta40459));
});

}

return (new cljs.core.async.t40458(cljs$core$async$map_GT_,f,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.filter_GT_ = (function cljs$core$async$filter_GT_(p,ch){
if(typeof cljs.core.async.t40464 !== 'undefined'){
} else {

/**
* @constructor
*/
cljs.core.async.t40464 = (function (filter_GT_,p,ch,meta40465){
this.filter_GT_ = filter_GT_;
this.p = p;
this.ch = ch;
this.meta40465 = meta40465;
this.cljs$lang$protocol_mask$partition0$ = 393216;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
cljs.core.async.t40464.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_40466,meta40465__$1){
var self__ = this;
var _40466__$1 = this;
return (new cljs.core.async.t40464(self__.filter_GT_,self__.p,self__.ch,meta40465__$1));
});

cljs.core.async.t40464.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_40466){
var self__ = this;
var _40466__$1 = this;
return self__.meta40465;
});

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$Channel$ = true;

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$Channel$close_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.close_BANG_.call(null,self__.ch);
});

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$Channel$closed_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch);
});

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$ReadPort$ = true;

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$ReadPort$take_BANG_$arity$2 = (function (_,fn1){
var self__ = this;
var ___$1 = this;
return cljs.core.async.impl.protocols.take_BANG_.call(null,self__.ch,fn1);
});

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$WritePort$ = true;

cljs.core.async.t40464.prototype.cljs$core$async$impl$protocols$WritePort$put_BANG_$arity$3 = (function (_,val,fn1){
var self__ = this;
var ___$1 = this;
if(cljs.core.truth_(self__.p.call(null,val))){
return cljs.core.async.impl.protocols.put_BANG_.call(null,self__.ch,val,fn1);
} else {
return cljs.core.async.impl.channels.box.call(null,cljs.core.not.call(null,cljs.core.async.impl.protocols.closed_QMARK_.call(null,self__.ch)));
}
});

cljs.core.async.t40464.getBasis = (function (){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"filter>","filter>",-37644455,null),new cljs.core.Symbol(null,"p","p",1791580836,null),new cljs.core.Symbol(null,"ch","ch",1085813622,null),new cljs.core.Symbol(null,"meta40465","meta40465",-862596241,null)], null);
});

cljs.core.async.t40464.cljs$lang$type = true;

cljs.core.async.t40464.cljs$lang$ctorStr = "cljs.core.async/t40464";

cljs.core.async.t40464.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"cljs.core.async/t40464");
});

cljs.core.async.__GT_t40464 = (function cljs$core$async$filter_GT__$___GT_t40464(filter_GT___$1,p__$1,ch__$1,meta40465){
return (new cljs.core.async.t40464(filter_GT___$1,p__$1,ch__$1,meta40465));
});

}

return (new cljs.core.async.t40464(cljs$core$async$filter_GT_,p,ch,cljs.core.PersistentArrayMap.EMPTY));
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.remove_GT_ = (function cljs$core$async$remove_GT_(p,ch){
return cljs.core.async.filter_GT_.call(null,cljs.core.complement.call(null,p),ch);
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.filter_LT_ = (function cljs$core$async$filter_LT_(){
var G__40468 = arguments.length;
switch (G__40468) {
case 2:
return cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.filter_LT_.call(null,p,ch,null);
});

cljs.core.async.filter_LT_.cljs$core$IFn$_invoke$arity$3 = (function (p,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40511 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40511,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40511,out){
return (function (state_40489){
var state_val_40490 = (state_40489[(1)]);
if((state_val_40490 === (7))){
var inst_40485 = (state_40489[(2)]);
var state_40489__$1 = state_40489;
var statearr_40491_40512 = state_40489__$1;
(statearr_40491_40512[(2)] = inst_40485);

(statearr_40491_40512[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (1))){
var state_40489__$1 = state_40489;
var statearr_40492_40513 = state_40489__$1;
(statearr_40492_40513[(2)] = null);

(statearr_40492_40513[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (4))){
var inst_40471 = (state_40489[(7)]);
var inst_40471__$1 = (state_40489[(2)]);
var inst_40472 = (inst_40471__$1 == null);
var state_40489__$1 = (function (){var statearr_40493 = state_40489;
(statearr_40493[(7)] = inst_40471__$1);

return statearr_40493;
})();
if(cljs.core.truth_(inst_40472)){
var statearr_40494_40514 = state_40489__$1;
(statearr_40494_40514[(1)] = (5));

} else {
var statearr_40495_40515 = state_40489__$1;
(statearr_40495_40515[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (6))){
var inst_40471 = (state_40489[(7)]);
var inst_40476 = p.call(null,inst_40471);
var state_40489__$1 = state_40489;
if(cljs.core.truth_(inst_40476)){
var statearr_40496_40516 = state_40489__$1;
(statearr_40496_40516[(1)] = (8));

} else {
var statearr_40497_40517 = state_40489__$1;
(statearr_40497_40517[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (3))){
var inst_40487 = (state_40489[(2)]);
var state_40489__$1 = state_40489;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40489__$1,inst_40487);
} else {
if((state_val_40490 === (2))){
var state_40489__$1 = state_40489;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40489__$1,(4),ch);
} else {
if((state_val_40490 === (11))){
var inst_40479 = (state_40489[(2)]);
var state_40489__$1 = state_40489;
var statearr_40498_40518 = state_40489__$1;
(statearr_40498_40518[(2)] = inst_40479);

(statearr_40498_40518[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (9))){
var state_40489__$1 = state_40489;
var statearr_40499_40519 = state_40489__$1;
(statearr_40499_40519[(2)] = null);

(statearr_40499_40519[(1)] = (10));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (5))){
var inst_40474 = cljs.core.async.close_BANG_.call(null,out);
var state_40489__$1 = state_40489;
var statearr_40500_40520 = state_40489__$1;
(statearr_40500_40520[(2)] = inst_40474);

(statearr_40500_40520[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (10))){
var inst_40482 = (state_40489[(2)]);
var state_40489__$1 = (function (){var statearr_40501 = state_40489;
(statearr_40501[(8)] = inst_40482);

return statearr_40501;
})();
var statearr_40502_40521 = state_40489__$1;
(statearr_40502_40521[(2)] = null);

(statearr_40502_40521[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40490 === (8))){
var inst_40471 = (state_40489[(7)]);
var state_40489__$1 = state_40489;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40489__$1,(11),out,inst_40471);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40511,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40511,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40506 = [null,null,null,null,null,null,null,null,null];
(statearr_40506[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40506[(1)] = (1));

return statearr_40506;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40489){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40489);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40507){if((e40507 instanceof Object)){
var ex__23575__auto__ = e40507;
var statearr_40508_40522 = state_40489;
(statearr_40508_40522[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40489);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40507;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40523 = state_40489;
state_40489 = G__40523;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40489){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40489);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40511,out))
})();
var state__23635__auto__ = (function (){var statearr_40509 = f__23634__auto__.call(null);
(statearr_40509[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40511);

return statearr_40509;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40511,out))
);


return out;
});

cljs.core.async.filter_LT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.remove_LT_ = (function cljs$core$async$remove_LT_(){
var G__40525 = arguments.length;
switch (G__40525) {
case 2:
return cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$2 = (function (p,ch){
return cljs.core.async.remove_LT_.call(null,p,ch,null);
});

cljs.core.async.remove_LT_.cljs$core$IFn$_invoke$arity$3 = (function (p,ch,buf_or_n){
return cljs.core.async.filter_LT_.call(null,cljs.core.complement.call(null,p),ch,buf_or_n);
});

cljs.core.async.remove_LT_.cljs$lang$maxFixedArity = 3;
cljs.core.async.mapcat_STAR_ = (function cljs$core$async$mapcat_STAR_(f,in$,out){
var c__23633__auto__ = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto__){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto__){
return (function (state_40692){
var state_val_40693 = (state_40692[(1)]);
if((state_val_40693 === (7))){
var inst_40688 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
var statearr_40694_40735 = state_40692__$1;
(statearr_40694_40735[(2)] = inst_40688);

(statearr_40694_40735[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (20))){
var inst_40658 = (state_40692[(7)]);
var inst_40669 = (state_40692[(2)]);
var inst_40670 = cljs.core.next.call(null,inst_40658);
var inst_40644 = inst_40670;
var inst_40645 = null;
var inst_40646 = (0);
var inst_40647 = (0);
var state_40692__$1 = (function (){var statearr_40695 = state_40692;
(statearr_40695[(8)] = inst_40644);

(statearr_40695[(9)] = inst_40646);

(statearr_40695[(10)] = inst_40647);

(statearr_40695[(11)] = inst_40645);

(statearr_40695[(12)] = inst_40669);

return statearr_40695;
})();
var statearr_40696_40736 = state_40692__$1;
(statearr_40696_40736[(2)] = null);

(statearr_40696_40736[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (1))){
var state_40692__$1 = state_40692;
var statearr_40697_40737 = state_40692__$1;
(statearr_40697_40737[(2)] = null);

(statearr_40697_40737[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (4))){
var inst_40633 = (state_40692[(13)]);
var inst_40633__$1 = (state_40692[(2)]);
var inst_40634 = (inst_40633__$1 == null);
var state_40692__$1 = (function (){var statearr_40698 = state_40692;
(statearr_40698[(13)] = inst_40633__$1);

return statearr_40698;
})();
if(cljs.core.truth_(inst_40634)){
var statearr_40699_40738 = state_40692__$1;
(statearr_40699_40738[(1)] = (5));

} else {
var statearr_40700_40739 = state_40692__$1;
(statearr_40700_40739[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (15))){
var state_40692__$1 = state_40692;
var statearr_40704_40740 = state_40692__$1;
(statearr_40704_40740[(2)] = null);

(statearr_40704_40740[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (21))){
var state_40692__$1 = state_40692;
var statearr_40705_40741 = state_40692__$1;
(statearr_40705_40741[(2)] = null);

(statearr_40705_40741[(1)] = (23));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (13))){
var inst_40644 = (state_40692[(8)]);
var inst_40646 = (state_40692[(9)]);
var inst_40647 = (state_40692[(10)]);
var inst_40645 = (state_40692[(11)]);
var inst_40654 = (state_40692[(2)]);
var inst_40655 = (inst_40647 + (1));
var tmp40701 = inst_40644;
var tmp40702 = inst_40646;
var tmp40703 = inst_40645;
var inst_40644__$1 = tmp40701;
var inst_40645__$1 = tmp40703;
var inst_40646__$1 = tmp40702;
var inst_40647__$1 = inst_40655;
var state_40692__$1 = (function (){var statearr_40706 = state_40692;
(statearr_40706[(8)] = inst_40644__$1);

(statearr_40706[(14)] = inst_40654);

(statearr_40706[(9)] = inst_40646__$1);

(statearr_40706[(10)] = inst_40647__$1);

(statearr_40706[(11)] = inst_40645__$1);

return statearr_40706;
})();
var statearr_40707_40742 = state_40692__$1;
(statearr_40707_40742[(2)] = null);

(statearr_40707_40742[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (22))){
var state_40692__$1 = state_40692;
var statearr_40708_40743 = state_40692__$1;
(statearr_40708_40743[(2)] = null);

(statearr_40708_40743[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (6))){
var inst_40633 = (state_40692[(13)]);
var inst_40642 = f.call(null,inst_40633);
var inst_40643 = cljs.core.seq.call(null,inst_40642);
var inst_40644 = inst_40643;
var inst_40645 = null;
var inst_40646 = (0);
var inst_40647 = (0);
var state_40692__$1 = (function (){var statearr_40709 = state_40692;
(statearr_40709[(8)] = inst_40644);

(statearr_40709[(9)] = inst_40646);

(statearr_40709[(10)] = inst_40647);

(statearr_40709[(11)] = inst_40645);

return statearr_40709;
})();
var statearr_40710_40744 = state_40692__$1;
(statearr_40710_40744[(2)] = null);

(statearr_40710_40744[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (17))){
var inst_40658 = (state_40692[(7)]);
var inst_40662 = cljs.core.chunk_first.call(null,inst_40658);
var inst_40663 = cljs.core.chunk_rest.call(null,inst_40658);
var inst_40664 = cljs.core.count.call(null,inst_40662);
var inst_40644 = inst_40663;
var inst_40645 = inst_40662;
var inst_40646 = inst_40664;
var inst_40647 = (0);
var state_40692__$1 = (function (){var statearr_40711 = state_40692;
(statearr_40711[(8)] = inst_40644);

(statearr_40711[(9)] = inst_40646);

(statearr_40711[(10)] = inst_40647);

(statearr_40711[(11)] = inst_40645);

return statearr_40711;
})();
var statearr_40712_40745 = state_40692__$1;
(statearr_40712_40745[(2)] = null);

(statearr_40712_40745[(1)] = (8));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (3))){
var inst_40690 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40692__$1,inst_40690);
} else {
if((state_val_40693 === (12))){
var inst_40678 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
var statearr_40713_40746 = state_40692__$1;
(statearr_40713_40746[(2)] = inst_40678);

(statearr_40713_40746[(1)] = (9));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (2))){
var state_40692__$1 = state_40692;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40692__$1,(4),in$);
} else {
if((state_val_40693 === (23))){
var inst_40686 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
var statearr_40714_40747 = state_40692__$1;
(statearr_40714_40747[(2)] = inst_40686);

(statearr_40714_40747[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (19))){
var inst_40673 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
var statearr_40715_40748 = state_40692__$1;
(statearr_40715_40748[(2)] = inst_40673);

(statearr_40715_40748[(1)] = (16));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (11))){
var inst_40644 = (state_40692[(8)]);
var inst_40658 = (state_40692[(7)]);
var inst_40658__$1 = cljs.core.seq.call(null,inst_40644);
var state_40692__$1 = (function (){var statearr_40716 = state_40692;
(statearr_40716[(7)] = inst_40658__$1);

return statearr_40716;
})();
if(inst_40658__$1){
var statearr_40717_40749 = state_40692__$1;
(statearr_40717_40749[(1)] = (14));

} else {
var statearr_40718_40750 = state_40692__$1;
(statearr_40718_40750[(1)] = (15));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (9))){
var inst_40680 = (state_40692[(2)]);
var inst_40681 = cljs.core.async.impl.protocols.closed_QMARK_.call(null,out);
var state_40692__$1 = (function (){var statearr_40719 = state_40692;
(statearr_40719[(15)] = inst_40680);

return statearr_40719;
})();
if(cljs.core.truth_(inst_40681)){
var statearr_40720_40751 = state_40692__$1;
(statearr_40720_40751[(1)] = (21));

} else {
var statearr_40721_40752 = state_40692__$1;
(statearr_40721_40752[(1)] = (22));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (5))){
var inst_40636 = cljs.core.async.close_BANG_.call(null,out);
var state_40692__$1 = state_40692;
var statearr_40722_40753 = state_40692__$1;
(statearr_40722_40753[(2)] = inst_40636);

(statearr_40722_40753[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (14))){
var inst_40658 = (state_40692[(7)]);
var inst_40660 = cljs.core.chunked_seq_QMARK_.call(null,inst_40658);
var state_40692__$1 = state_40692;
if(inst_40660){
var statearr_40723_40754 = state_40692__$1;
(statearr_40723_40754[(1)] = (17));

} else {
var statearr_40724_40755 = state_40692__$1;
(statearr_40724_40755[(1)] = (18));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (16))){
var inst_40676 = (state_40692[(2)]);
var state_40692__$1 = state_40692;
var statearr_40725_40756 = state_40692__$1;
(statearr_40725_40756[(2)] = inst_40676);

(statearr_40725_40756[(1)] = (12));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40693 === (10))){
var inst_40647 = (state_40692[(10)]);
var inst_40645 = (state_40692[(11)]);
var inst_40652 = cljs.core._nth.call(null,inst_40645,inst_40647);
var state_40692__$1 = state_40692;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40692__$1,(13),out,inst_40652);
} else {
if((state_val_40693 === (18))){
var inst_40658 = (state_40692[(7)]);
var inst_40667 = cljs.core.first.call(null,inst_40658);
var state_40692__$1 = state_40692;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40692__$1,(20),out,inst_40667);
} else {
if((state_val_40693 === (8))){
var inst_40646 = (state_40692[(9)]);
var inst_40647 = (state_40692[(10)]);
var inst_40649 = (inst_40647 < inst_40646);
var inst_40650 = inst_40649;
var state_40692__$1 = state_40692;
if(cljs.core.truth_(inst_40650)){
var statearr_40726_40757 = state_40692__$1;
(statearr_40726_40757[(1)] = (10));

} else {
var statearr_40727_40758 = state_40692__$1;
(statearr_40727_40758[(1)] = (11));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto__))
;
return ((function (switch__23571__auto__,c__23633__auto__){
return (function() {
var cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__ = null;
var cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____0 = (function (){
var statearr_40731 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40731[(0)] = cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__);

(statearr_40731[(1)] = (1));

return statearr_40731;
});
var cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____1 = (function (state_40692){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40692);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40732){if((e40732 instanceof Object)){
var ex__23575__auto__ = e40732;
var statearr_40733_40759 = state_40692;
(statearr_40733_40759[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40692);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40732;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40760 = state_40692;
state_40692 = G__40760;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__ = function(state_40692){
switch(arguments.length){
case 0:
return cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____1.call(this,state_40692);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____0;
cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$mapcat_STAR__$_state_machine__23572__auto____1;
return cljs$core$async$mapcat_STAR__$_state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto__))
})();
var state__23635__auto__ = (function (){var statearr_40734 = f__23634__auto__.call(null);
(statearr_40734[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto__);

return statearr_40734;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto__))
);

return c__23633__auto__;
});
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.mapcat_LT_ = (function cljs$core$async$mapcat_LT_(){
var G__40762 = arguments.length;
switch (G__40762) {
case 2:
return cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$2 = (function (f,in$){
return cljs.core.async.mapcat_LT_.call(null,f,in$,null);
});

cljs.core.async.mapcat_LT_.cljs$core$IFn$_invoke$arity$3 = (function (f,in$,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
cljs.core.async.mapcat_STAR_.call(null,f,in$,out);

return out;
});

cljs.core.async.mapcat_LT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.mapcat_GT_ = (function cljs$core$async$mapcat_GT_(){
var G__40765 = arguments.length;
switch (G__40765) {
case 2:
return cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$2 = (function (f,out){
return cljs.core.async.mapcat_GT_.call(null,f,out,null);
});

cljs.core.async.mapcat_GT_.cljs$core$IFn$_invoke$arity$3 = (function (f,out,buf_or_n){
var in$ = cljs.core.async.chan.call(null,buf_or_n);
cljs.core.async.mapcat_STAR_.call(null,f,in$,out);

return in$;
});

cljs.core.async.mapcat_GT_.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.unique = (function cljs$core$async$unique(){
var G__40768 = arguments.length;
switch (G__40768) {
case 1:
return cljs.core.async.unique.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return cljs.core.async.unique.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.unique.cljs$core$IFn$_invoke$arity$1 = (function (ch){
return cljs.core.async.unique.call(null,ch,null);
});

cljs.core.async.unique.cljs$core$IFn$_invoke$arity$2 = (function (ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40818 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40818,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40818,out){
return (function (state_40792){
var state_val_40793 = (state_40792[(1)]);
if((state_val_40793 === (7))){
var inst_40787 = (state_40792[(2)]);
var state_40792__$1 = state_40792;
var statearr_40794_40819 = state_40792__$1;
(statearr_40794_40819[(2)] = inst_40787);

(statearr_40794_40819[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (1))){
var inst_40769 = null;
var state_40792__$1 = (function (){var statearr_40795 = state_40792;
(statearr_40795[(7)] = inst_40769);

return statearr_40795;
})();
var statearr_40796_40820 = state_40792__$1;
(statearr_40796_40820[(2)] = null);

(statearr_40796_40820[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (4))){
var inst_40772 = (state_40792[(8)]);
var inst_40772__$1 = (state_40792[(2)]);
var inst_40773 = (inst_40772__$1 == null);
var inst_40774 = cljs.core.not.call(null,inst_40773);
var state_40792__$1 = (function (){var statearr_40797 = state_40792;
(statearr_40797[(8)] = inst_40772__$1);

return statearr_40797;
})();
if(inst_40774){
var statearr_40798_40821 = state_40792__$1;
(statearr_40798_40821[(1)] = (5));

} else {
var statearr_40799_40822 = state_40792__$1;
(statearr_40799_40822[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (6))){
var state_40792__$1 = state_40792;
var statearr_40800_40823 = state_40792__$1;
(statearr_40800_40823[(2)] = null);

(statearr_40800_40823[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (3))){
var inst_40789 = (state_40792[(2)]);
var inst_40790 = cljs.core.async.close_BANG_.call(null,out);
var state_40792__$1 = (function (){var statearr_40801 = state_40792;
(statearr_40801[(9)] = inst_40789);

return statearr_40801;
})();
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40792__$1,inst_40790);
} else {
if((state_val_40793 === (2))){
var state_40792__$1 = state_40792;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40792__$1,(4),ch);
} else {
if((state_val_40793 === (11))){
var inst_40772 = (state_40792[(8)]);
var inst_40781 = (state_40792[(2)]);
var inst_40769 = inst_40772;
var state_40792__$1 = (function (){var statearr_40802 = state_40792;
(statearr_40802[(10)] = inst_40781);

(statearr_40802[(7)] = inst_40769);

return statearr_40802;
})();
var statearr_40803_40824 = state_40792__$1;
(statearr_40803_40824[(2)] = null);

(statearr_40803_40824[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (9))){
var inst_40772 = (state_40792[(8)]);
var state_40792__$1 = state_40792;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40792__$1,(11),out,inst_40772);
} else {
if((state_val_40793 === (5))){
var inst_40772 = (state_40792[(8)]);
var inst_40769 = (state_40792[(7)]);
var inst_40776 = cljs.core._EQ_.call(null,inst_40772,inst_40769);
var state_40792__$1 = state_40792;
if(inst_40776){
var statearr_40805_40825 = state_40792__$1;
(statearr_40805_40825[(1)] = (8));

} else {
var statearr_40806_40826 = state_40792__$1;
(statearr_40806_40826[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (10))){
var inst_40784 = (state_40792[(2)]);
var state_40792__$1 = state_40792;
var statearr_40807_40827 = state_40792__$1;
(statearr_40807_40827[(2)] = inst_40784);

(statearr_40807_40827[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40793 === (8))){
var inst_40769 = (state_40792[(7)]);
var tmp40804 = inst_40769;
var inst_40769__$1 = tmp40804;
var state_40792__$1 = (function (){var statearr_40808 = state_40792;
(statearr_40808[(7)] = inst_40769__$1);

return statearr_40808;
})();
var statearr_40809_40828 = state_40792__$1;
(statearr_40809_40828[(2)] = null);

(statearr_40809_40828[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40818,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40818,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40813 = [null,null,null,null,null,null,null,null,null,null,null];
(statearr_40813[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40813[(1)] = (1));

return statearr_40813;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40792){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40792);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40814){if((e40814 instanceof Object)){
var ex__23575__auto__ = e40814;
var statearr_40815_40829 = state_40792;
(statearr_40815_40829[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40792);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40814;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40830 = state_40792;
state_40792 = G__40830;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40792){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40792);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40818,out))
})();
var state__23635__auto__ = (function (){var statearr_40816 = f__23634__auto__.call(null);
(statearr_40816[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40818);

return statearr_40816;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40818,out))
);


return out;
});

cljs.core.async.unique.cljs$lang$maxFixedArity = 2;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.partition = (function cljs$core$async$partition(){
var G__40832 = arguments.length;
switch (G__40832) {
case 2:
return cljs.core.async.partition.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.partition.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.partition.cljs$core$IFn$_invoke$arity$2 = (function (n,ch){
return cljs.core.async.partition.call(null,n,ch,null);
});

cljs.core.async.partition.cljs$core$IFn$_invoke$arity$3 = (function (n,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40901 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40901,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40901,out){
return (function (state_40870){
var state_val_40871 = (state_40870[(1)]);
if((state_val_40871 === (7))){
var inst_40866 = (state_40870[(2)]);
var state_40870__$1 = state_40870;
var statearr_40872_40902 = state_40870__$1;
(statearr_40872_40902[(2)] = inst_40866);

(statearr_40872_40902[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (1))){
var inst_40833 = (new Array(n));
var inst_40834 = inst_40833;
var inst_40835 = (0);
var state_40870__$1 = (function (){var statearr_40873 = state_40870;
(statearr_40873[(7)] = inst_40835);

(statearr_40873[(8)] = inst_40834);

return statearr_40873;
})();
var statearr_40874_40903 = state_40870__$1;
(statearr_40874_40903[(2)] = null);

(statearr_40874_40903[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (4))){
var inst_40838 = (state_40870[(9)]);
var inst_40838__$1 = (state_40870[(2)]);
var inst_40839 = (inst_40838__$1 == null);
var inst_40840 = cljs.core.not.call(null,inst_40839);
var state_40870__$1 = (function (){var statearr_40875 = state_40870;
(statearr_40875[(9)] = inst_40838__$1);

return statearr_40875;
})();
if(inst_40840){
var statearr_40876_40904 = state_40870__$1;
(statearr_40876_40904[(1)] = (5));

} else {
var statearr_40877_40905 = state_40870__$1;
(statearr_40877_40905[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (15))){
var inst_40860 = (state_40870[(2)]);
var state_40870__$1 = state_40870;
var statearr_40878_40906 = state_40870__$1;
(statearr_40878_40906[(2)] = inst_40860);

(statearr_40878_40906[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (13))){
var state_40870__$1 = state_40870;
var statearr_40879_40907 = state_40870__$1;
(statearr_40879_40907[(2)] = null);

(statearr_40879_40907[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (6))){
var inst_40835 = (state_40870[(7)]);
var inst_40856 = (inst_40835 > (0));
var state_40870__$1 = state_40870;
if(cljs.core.truth_(inst_40856)){
var statearr_40880_40908 = state_40870__$1;
(statearr_40880_40908[(1)] = (12));

} else {
var statearr_40881_40909 = state_40870__$1;
(statearr_40881_40909[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (3))){
var inst_40868 = (state_40870[(2)]);
var state_40870__$1 = state_40870;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40870__$1,inst_40868);
} else {
if((state_val_40871 === (12))){
var inst_40834 = (state_40870[(8)]);
var inst_40858 = cljs.core.vec.call(null,inst_40834);
var state_40870__$1 = state_40870;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40870__$1,(15),out,inst_40858);
} else {
if((state_val_40871 === (2))){
var state_40870__$1 = state_40870;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40870__$1,(4),ch);
} else {
if((state_val_40871 === (11))){
var inst_40850 = (state_40870[(2)]);
var inst_40851 = (new Array(n));
var inst_40834 = inst_40851;
var inst_40835 = (0);
var state_40870__$1 = (function (){var statearr_40882 = state_40870;
(statearr_40882[(7)] = inst_40835);

(statearr_40882[(8)] = inst_40834);

(statearr_40882[(10)] = inst_40850);

return statearr_40882;
})();
var statearr_40883_40910 = state_40870__$1;
(statearr_40883_40910[(2)] = null);

(statearr_40883_40910[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (9))){
var inst_40834 = (state_40870[(8)]);
var inst_40848 = cljs.core.vec.call(null,inst_40834);
var state_40870__$1 = state_40870;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40870__$1,(11),out,inst_40848);
} else {
if((state_val_40871 === (5))){
var inst_40835 = (state_40870[(7)]);
var inst_40834 = (state_40870[(8)]);
var inst_40838 = (state_40870[(9)]);
var inst_40843 = (state_40870[(11)]);
var inst_40842 = (inst_40834[inst_40835] = inst_40838);
var inst_40843__$1 = (inst_40835 + (1));
var inst_40844 = (inst_40843__$1 < n);
var state_40870__$1 = (function (){var statearr_40884 = state_40870;
(statearr_40884[(11)] = inst_40843__$1);

(statearr_40884[(12)] = inst_40842);

return statearr_40884;
})();
if(cljs.core.truth_(inst_40844)){
var statearr_40885_40911 = state_40870__$1;
(statearr_40885_40911[(1)] = (8));

} else {
var statearr_40886_40912 = state_40870__$1;
(statearr_40886_40912[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (14))){
var inst_40863 = (state_40870[(2)]);
var inst_40864 = cljs.core.async.close_BANG_.call(null,out);
var state_40870__$1 = (function (){var statearr_40888 = state_40870;
(statearr_40888[(13)] = inst_40863);

return statearr_40888;
})();
var statearr_40889_40913 = state_40870__$1;
(statearr_40889_40913[(2)] = inst_40864);

(statearr_40889_40913[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (10))){
var inst_40854 = (state_40870[(2)]);
var state_40870__$1 = state_40870;
var statearr_40890_40914 = state_40870__$1;
(statearr_40890_40914[(2)] = inst_40854);

(statearr_40890_40914[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40871 === (8))){
var inst_40834 = (state_40870[(8)]);
var inst_40843 = (state_40870[(11)]);
var tmp40887 = inst_40834;
var inst_40834__$1 = tmp40887;
var inst_40835 = inst_40843;
var state_40870__$1 = (function (){var statearr_40891 = state_40870;
(statearr_40891[(7)] = inst_40835);

(statearr_40891[(8)] = inst_40834__$1);

return statearr_40891;
})();
var statearr_40892_40915 = state_40870__$1;
(statearr_40892_40915[(2)] = null);

(statearr_40892_40915[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40901,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40901,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40896 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40896[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40896[(1)] = (1));

return statearr_40896;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40870){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40870);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40897){if((e40897 instanceof Object)){
var ex__23575__auto__ = e40897;
var statearr_40898_40916 = state_40870;
(statearr_40898_40916[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40870);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40897;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__40917 = state_40870;
state_40870 = G__40917;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40870){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40870);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40901,out))
})();
var state__23635__auto__ = (function (){var statearr_40899 = f__23634__auto__.call(null);
(statearr_40899[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40901);

return statearr_40899;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40901,out))
);


return out;
});

cljs.core.async.partition.cljs$lang$maxFixedArity = 3;
/**
 * Deprecated - this function will be removed. Use transducer instead
 */
cljs.core.async.partition_by = (function cljs$core$async$partition_by(){
var G__40919 = arguments.length;
switch (G__40919) {
case 2:
return cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$2 = (function (f,ch){
return cljs.core.async.partition_by.call(null,f,ch,null);
});

cljs.core.async.partition_by.cljs$core$IFn$_invoke$arity$3 = (function (f,ch,buf_or_n){
var out = cljs.core.async.chan.call(null,buf_or_n);
var c__23633__auto___40992 = cljs.core.async.chan.call(null,(1));
cljs.core.async.impl.dispatch.run.call(null,((function (c__23633__auto___40992,out){
return (function (){
var f__23634__auto__ = (function (){var switch__23571__auto__ = ((function (c__23633__auto___40992,out){
return (function (state_40961){
var state_val_40962 = (state_40961[(1)]);
if((state_val_40962 === (7))){
var inst_40957 = (state_40961[(2)]);
var state_40961__$1 = state_40961;
var statearr_40963_40993 = state_40961__$1;
(statearr_40963_40993[(2)] = inst_40957);

(statearr_40963_40993[(1)] = (3));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (1))){
var inst_40920 = [];
var inst_40921 = inst_40920;
var inst_40922 = new cljs.core.Keyword("cljs.core.async","nothing","cljs.core.async/nothing",-69252123);
var state_40961__$1 = (function (){var statearr_40964 = state_40961;
(statearr_40964[(7)] = inst_40922);

(statearr_40964[(8)] = inst_40921);

return statearr_40964;
})();
var statearr_40965_40994 = state_40961__$1;
(statearr_40965_40994[(2)] = null);

(statearr_40965_40994[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (4))){
var inst_40925 = (state_40961[(9)]);
var inst_40925__$1 = (state_40961[(2)]);
var inst_40926 = (inst_40925__$1 == null);
var inst_40927 = cljs.core.not.call(null,inst_40926);
var state_40961__$1 = (function (){var statearr_40966 = state_40961;
(statearr_40966[(9)] = inst_40925__$1);

return statearr_40966;
})();
if(inst_40927){
var statearr_40967_40995 = state_40961__$1;
(statearr_40967_40995[(1)] = (5));

} else {
var statearr_40968_40996 = state_40961__$1;
(statearr_40968_40996[(1)] = (6));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (15))){
var inst_40951 = (state_40961[(2)]);
var state_40961__$1 = state_40961;
var statearr_40969_40997 = state_40961__$1;
(statearr_40969_40997[(2)] = inst_40951);

(statearr_40969_40997[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (13))){
var state_40961__$1 = state_40961;
var statearr_40970_40998 = state_40961__$1;
(statearr_40970_40998[(2)] = null);

(statearr_40970_40998[(1)] = (14));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (6))){
var inst_40921 = (state_40961[(8)]);
var inst_40946 = inst_40921.length;
var inst_40947 = (inst_40946 > (0));
var state_40961__$1 = state_40961;
if(cljs.core.truth_(inst_40947)){
var statearr_40971_40999 = state_40961__$1;
(statearr_40971_40999[(1)] = (12));

} else {
var statearr_40972_41000 = state_40961__$1;
(statearr_40972_41000[(1)] = (13));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (3))){
var inst_40959 = (state_40961[(2)]);
var state_40961__$1 = state_40961;
return cljs.core.async.impl.ioc_helpers.return_chan.call(null,state_40961__$1,inst_40959);
} else {
if((state_val_40962 === (12))){
var inst_40921 = (state_40961[(8)]);
var inst_40949 = cljs.core.vec.call(null,inst_40921);
var state_40961__$1 = state_40961;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40961__$1,(15),out,inst_40949);
} else {
if((state_val_40962 === (2))){
var state_40961__$1 = state_40961;
return cljs.core.async.impl.ioc_helpers.take_BANG_.call(null,state_40961__$1,(4),ch);
} else {
if((state_val_40962 === (11))){
var inst_40929 = (state_40961[(10)]);
var inst_40925 = (state_40961[(9)]);
var inst_40939 = (state_40961[(2)]);
var inst_40940 = [];
var inst_40941 = inst_40940.push(inst_40925);
var inst_40921 = inst_40940;
var inst_40922 = inst_40929;
var state_40961__$1 = (function (){var statearr_40973 = state_40961;
(statearr_40973[(7)] = inst_40922);

(statearr_40973[(11)] = inst_40939);

(statearr_40973[(12)] = inst_40941);

(statearr_40973[(8)] = inst_40921);

return statearr_40973;
})();
var statearr_40974_41001 = state_40961__$1;
(statearr_40974_41001[(2)] = null);

(statearr_40974_41001[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (9))){
var inst_40921 = (state_40961[(8)]);
var inst_40937 = cljs.core.vec.call(null,inst_40921);
var state_40961__$1 = state_40961;
return cljs.core.async.impl.ioc_helpers.put_BANG_.call(null,state_40961__$1,(11),out,inst_40937);
} else {
if((state_val_40962 === (5))){
var inst_40922 = (state_40961[(7)]);
var inst_40929 = (state_40961[(10)]);
var inst_40925 = (state_40961[(9)]);
var inst_40929__$1 = f.call(null,inst_40925);
var inst_40930 = cljs.core._EQ_.call(null,inst_40929__$1,inst_40922);
var inst_40931 = cljs.core.keyword_identical_QMARK_.call(null,inst_40922,new cljs.core.Keyword("cljs.core.async","nothing","cljs.core.async/nothing",-69252123));
var inst_40932 = (inst_40930) || (inst_40931);
var state_40961__$1 = (function (){var statearr_40975 = state_40961;
(statearr_40975[(10)] = inst_40929__$1);

return statearr_40975;
})();
if(cljs.core.truth_(inst_40932)){
var statearr_40976_41002 = state_40961__$1;
(statearr_40976_41002[(1)] = (8));

} else {
var statearr_40977_41003 = state_40961__$1;
(statearr_40977_41003[(1)] = (9));

}

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (14))){
var inst_40954 = (state_40961[(2)]);
var inst_40955 = cljs.core.async.close_BANG_.call(null,out);
var state_40961__$1 = (function (){var statearr_40979 = state_40961;
(statearr_40979[(13)] = inst_40954);

return statearr_40979;
})();
var statearr_40980_41004 = state_40961__$1;
(statearr_40980_41004[(2)] = inst_40955);

(statearr_40980_41004[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (10))){
var inst_40944 = (state_40961[(2)]);
var state_40961__$1 = state_40961;
var statearr_40981_41005 = state_40961__$1;
(statearr_40981_41005[(2)] = inst_40944);

(statearr_40981_41005[(1)] = (7));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
if((state_val_40962 === (8))){
var inst_40929 = (state_40961[(10)]);
var inst_40925 = (state_40961[(9)]);
var inst_40921 = (state_40961[(8)]);
var inst_40934 = inst_40921.push(inst_40925);
var tmp40978 = inst_40921;
var inst_40921__$1 = tmp40978;
var inst_40922 = inst_40929;
var state_40961__$1 = (function (){var statearr_40982 = state_40961;
(statearr_40982[(7)] = inst_40922);

(statearr_40982[(8)] = inst_40921__$1);

(statearr_40982[(14)] = inst_40934);

return statearr_40982;
})();
var statearr_40983_41006 = state_40961__$1;
(statearr_40983_41006[(2)] = null);

(statearr_40983_41006[(1)] = (2));


return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
return null;
}
}
}
}
}
}
}
}
}
}
}
}
}
}
}
});})(c__23633__auto___40992,out))
;
return ((function (switch__23571__auto__,c__23633__auto___40992,out){
return (function() {
var cljs$core$async$state_machine__23572__auto__ = null;
var cljs$core$async$state_machine__23572__auto____0 = (function (){
var statearr_40987 = [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null];
(statearr_40987[(0)] = cljs$core$async$state_machine__23572__auto__);

(statearr_40987[(1)] = (1));

return statearr_40987;
});
var cljs$core$async$state_machine__23572__auto____1 = (function (state_40961){
while(true){
var ret_value__23573__auto__ = (function (){try{while(true){
var result__23574__auto__ = switch__23571__auto__.call(null,state_40961);
if(cljs.core.keyword_identical_QMARK_.call(null,result__23574__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
continue;
} else {
return result__23574__auto__;
}
break;
}
}catch (e40988){if((e40988 instanceof Object)){
var ex__23575__auto__ = e40988;
var statearr_40989_41007 = state_40961;
(statearr_40989_41007[(5)] = ex__23575__auto__);


cljs.core.async.impl.ioc_helpers.process_exception.call(null,state_40961);

return new cljs.core.Keyword(null,"recur","recur",-437573268);
} else {
throw e40988;

}
}})();
if(cljs.core.keyword_identical_QMARK_.call(null,ret_value__23573__auto__,new cljs.core.Keyword(null,"recur","recur",-437573268))){
var G__41008 = state_40961;
state_40961 = G__41008;
continue;
} else {
return ret_value__23573__auto__;
}
break;
}
});
cljs$core$async$state_machine__23572__auto__ = function(state_40961){
switch(arguments.length){
case 0:
return cljs$core$async$state_machine__23572__auto____0.call(this);
case 1:
return cljs$core$async$state_machine__23572__auto____1.call(this,state_40961);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$0 = cljs$core$async$state_machine__23572__auto____0;
cljs$core$async$state_machine__23572__auto__.cljs$core$IFn$_invoke$arity$1 = cljs$core$async$state_machine__23572__auto____1;
return cljs$core$async$state_machine__23572__auto__;
})()
;})(switch__23571__auto__,c__23633__auto___40992,out))
})();
var state__23635__auto__ = (function (){var statearr_40990 = f__23634__auto__.call(null);
(statearr_40990[cljs.core.async.impl.ioc_helpers.USER_START_IDX] = c__23633__auto___40992);

return statearr_40990;
})();
return cljs.core.async.impl.ioc_helpers.run_state_machine_wrapped.call(null,state__23635__auto__);
});})(c__23633__auto___40992,out))
);


return out;
});

cljs.core.async.partition_by.cljs$lang$maxFixedArity = 3;

//# sourceMappingURL=async.js.map?rel=1439206057620