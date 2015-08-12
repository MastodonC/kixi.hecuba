// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.geom.core.vector');
goog.require('cljs.core');
goog.require('thi.ng.geom.core');
goog.require('thi.ng.math.core');
goog.require('thi.ng.common.error');









/**
* @constructor
*/
thi.ng.geom.core.vector.Vec2 = (function (buf,_hash,_meta){
this.buf = buf;
this._hash = _hash;
this._meta = _meta;
this.cljs$lang$protocol_mask$partition0$ = 166618075;
this.cljs$lang$protocol_mask$partition1$ = 10240;
})
thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PBuffered$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PBuffered$get_buffer$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.buf;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PBuffered$copy_to_buffer$arity$4 = (function (_,dest,stride,idx){
var self__ = this;
var ___$1 = this;
dest.set(self__.buf,idx);

return (idx + stride);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PTransform$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PTransform$transform$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.transform_vector.call(null,m,___$1);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PRotate$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PRotate$rotate$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
var b = (new Float32Array((2)));
var G__36901 = self__.buf;
var G__36902 = (G__36901[(0)]);
var G__36903 = (G__36901[(1)]);
(b[(0)] = ((G__36902 * c) - (G__36903 * s)));

(b[(1)] = ((G__36902 * s) + (G__36903 * c)));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.toString = (function (){
var self__ = this;
var _ = this;
return [cljs.core.str("["),cljs.core.str((self__.buf[(0)])),cljs.core.str(" "),cljs.core.str((self__.buf[(1)])),cljs.core.str("]")].join('');
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ILookup$_lookup$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,___$1);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ILookup$_lookup$arity$3 = (function (_,k,nf){
var self__ = this;
var ___$1 = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,___$1);
} else {
return nf;
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return nf;
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PDotProduct$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PDotProduct$dot$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__36904 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36905 = v.buf;
return (((G__36904[(0)]) * (G__36905[(0)])) + ((G__36904[(1)]) * (G__36905[(1)])));
} else {
return (((G__36904[(0)]) * cljs.core.nth.call(null,v,(0),0.0)) + ((G__36904[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormalize$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormalize$normalize$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__36906 = self__.buf;
var G__36907 = (G__36906[(0)]);
var G__36908 = (G__36906[(1)]);
var l = Math.sqrt(((G__36907 * G__36907) + (G__36908 * G__36908)));
if((l > (0))){
var b = (new Float32Array((2)));
(b[(0)] = (G__36907 / l));

(b[(1)] = (G__36908 / l));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormalize$normalize$arity$2 = (function (_,len){
var self__ = this;
var ___$1 = this;
var G__36909 = self__.buf;
var G__36910 = (G__36909[(0)]);
var G__36911 = (G__36909[(1)]);
var l = Math.sqrt(((G__36910 * G__36910) + (G__36911 * G__36911)));
if((l > (0))){
var l__$1 = (len / l);
var b = (new Float32Array((2)));
(b[(0)] = (G__36910 * l__$1));

(b[(1)] = (G__36911 * l__$1));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormalize$normalized_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,1.0,thi.ng.geom.core.mag_squared.call(null,___$1));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PClear$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PClear$clear_STAR_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec2((new Float32Array((2))),null,null));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PClear$clear_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = 0.0);

(self__.buf[(1)] = 0.0);

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PReflect$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PReflect$reflect$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
var G__36912 = self__.buf;
var G__36914 = (G__36912[(0)]);
var G__36915 = (G__36912[(1)]);
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36913 = v.buf;
var G__36916 = (G__36913[(0)]);
var G__36917 = (G__36913[(1)]);
var d = (((G__36914 * G__36916) + (G__36915 * G__36917)) + (2));
(b[(0)] = ((G__36916 * d) - G__36914));

(b[(1)] = ((G__36917 * d) - G__36915));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
} else {
var G__36916 = cljs.core.nth.call(null,v,(0),0.0);
var G__36917 = cljs.core.nth.call(null,v,(1),0.0);
var d = (((G__36914 * G__36916) + (G__36915 * G__36917)) + (2));
(b[(0)] = ((G__36916 * d) - G__36914));

(b[(1)] = ((G__36917 * d) - G__36915));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PCrossProduct$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PCrossProduct$cross$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__36918 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36919 = v.buf;
return (((G__36918[(0)]) * (G__36919[(1)])) - ((G__36918[(1)]) * (G__36919[(0)])));
} else {
return (((G__36918[(0)]) * cljs.core.nth.call(null,v,(1),0.0)) - ((G__36918[(1)]) * cljs.core.nth.call(null,v,(0),0.0)));
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IIndexed$_nth$arity$2 = (function (_,n){
var self__ = this;
var ___$1 = this;
if((n >= (0))){
if((n < (2))){
return (self__.buf[n]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,n);
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IIndexed$_nth$arity$3 = (function (_,n,nf){
var self__ = this;
var ___$1 = this;
if((n >= (0))){
if((n < (2))){
return (self__.buf[n]);
} else {
return nf;
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IVector$_assoc_n$arity$3 = (function (_,n,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array(self__.buf));
(b[n] = v);

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__._meta;
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ICloneable$_clone$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec2((new Float32Array(self__.buf)),self__._hash,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$INext$_next$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.cons.call(null,(self__.buf[(1)]),null);
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ICounted$_count$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (2);
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IStack$_peek$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (self__.buf[(1)]);
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IStack$_pop$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.with_meta.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [(self__.buf[(0)])], null),self__._meta);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = (- (self__.buf[(0)])));

(self__.buf[(1)] = (- (self__.buf[(1)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__36923_37250 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36924_37251 = v.buf;
(self__.buf[(0)] = ((G__36923_37250[(0)]) - (G__36924_37251[(0)])));

(self__.buf[(1)] = ((G__36923_37250[(1)]) - (G__36924_37251[(1)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__36923_37250[(0)]) - v));

(self__.buf[(1)] = ((G__36923_37250[(1)]) - v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__36923_37250[(0)]) - cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__36923_37250[(1)]) - cljs.core.nth.call(null,v,(1),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__36932_37252 = typeof v1 === 'number';
var G__36933_37253 = typeof v2 === 'number';
if(((G__36932_37252)?G__36933_37253:false)){
(self__.buf[(0)] = ((self__.buf[(0)]) - v1));

(self__.buf[(1)] = ((self__.buf[(1)]) - v2));
} else {
var G__36934_37254 = ((!(G__36932_37252))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__36935_37255 = ((!(G__36933_37253))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__36926_37256 = (cljs.core.truth_(G__36934_37254)?v1.buf:null);
var G__36927_37257 = (cljs.core.truth_(G__36935_37255)?v2.buf:null);
var G__36928_37258 = (cljs.core.truth_(G__36934_37254)?(G__36926_37256[(0)]):((G__36932_37252)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__36929_37259 = (cljs.core.truth_(G__36934_37254)?(G__36926_37256[(1)]):((G__36932_37252)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__36930_37260 = (cljs.core.truth_(G__36935_37255)?(G__36927_37257[(0)]):((G__36933_37253)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__36931_37261 = (cljs.core.truth_(G__36935_37255)?(G__36927_37257[(1)]):((G__36933_37253)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(self__.buf[(0)] = (((self__.buf[(0)]) - G__36928_37258) - G__36930_37260));

(self__.buf[(1)] = (((self__.buf[(1)]) - G__36929_37259) - G__36931_37261));
}

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__36936_37262 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36937_37263 = v.buf;
(self__.buf[(0)] = ((G__36936_37262[(0)]) * (G__36937_37263[(0)])));

(self__.buf[(1)] = ((G__36936_37262[(1)]) * (G__36937_37263[(1)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__36936_37262[(0)]) * v));

(self__.buf[(1)] = ((G__36936_37262[(1)]) * v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__36936_37262[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__36936_37262[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__36945_37264 = typeof v1 === 'number';
var G__36946_37265 = typeof v2 === 'number';
if(((G__36945_37264)?G__36946_37265:false)){
(self__.buf[(0)] = ((self__.buf[(0)]) * v1));

(self__.buf[(1)] = ((self__.buf[(1)]) * v2));
} else {
var G__36947_37266 = ((!(G__36945_37264))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__36948_37267 = ((!(G__36946_37265))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__36939_37268 = (cljs.core.truth_(G__36947_37266)?v1.buf:null);
var G__36940_37269 = (cljs.core.truth_(G__36948_37267)?v2.buf:null);
var G__36941_37270 = (cljs.core.truth_(G__36947_37266)?(G__36939_37268[(0)]):((G__36945_37264)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__36942_37271 = (cljs.core.truth_(G__36947_37266)?(G__36939_37268[(1)]):((G__36945_37264)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__36943_37272 = (cljs.core.truth_(G__36948_37267)?(G__36940_37269[(0)]):((G__36946_37265)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__36944_37273 = (cljs.core.truth_(G__36948_37267)?(G__36940_37269[(1)]):((G__36946_37265)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(self__.buf[(0)] = (((self__.buf[(0)]) * G__36941_37270) * G__36943_37272));

(self__.buf[(1)] = (((self__.buf[(1)]) * G__36942_37271) * G__36944_37273));
}

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$subm_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__36958_37274 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__36959_37275 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__36960_37276 = ((!(G__36958_37274))?typeof a === 'number':null);
var G__36961_37277 = ((!(G__36959_37275))?typeof b === 'number':null);
var G__36949_37278 = self__.buf;
var G__36950_37279 = ((G__36958_37274)?a.buf:null);
var G__36951_37280 = ((G__36959_37275)?b.buf:null);
var G__36952_37281 = (G__36949_37278[(0)]);
var G__36953_37282 = (G__36949_37278[(1)]);
var G__36954_37283 = ((G__36958_37274)?(G__36950_37279[(0)]):(cljs.core.truth_(G__36960_37276)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__36955_37284 = ((G__36958_37274)?(G__36950_37279[(1)]):(cljs.core.truth_(G__36960_37276)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__36956_37285 = ((G__36959_37275)?(G__36951_37280[(0)]):(cljs.core.truth_(G__36961_37277)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__36957_37286 = ((G__36959_37275)?(G__36951_37280[(1)]):(cljs.core.truth_(G__36961_37277)?b:cljs.core.nth.call(null,b,(1),1.0)));
(self__.buf[(0)] = ((G__36952_37281 - G__36954_37283) * G__36956_37285));

(self__.buf[(1)] = ((G__36953_37282 - G__36955_37284) * G__36957_37286));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$msub_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__36971_37287 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__36972_37288 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__36973_37289 = ((!(G__36971_37287))?typeof a === 'number':null);
var G__36974_37290 = ((!(G__36972_37288))?typeof b === 'number':null);
var G__36962_37291 = self__.buf;
var G__36963_37292 = ((G__36971_37287)?a.buf:null);
var G__36964_37293 = ((G__36972_37288)?b.buf:null);
var G__36965_37294 = (G__36962_37291[(0)]);
var G__36966_37295 = (G__36962_37291[(1)]);
var G__36967_37296 = ((G__36971_37287)?(G__36963_37292[(0)]):(cljs.core.truth_(G__36973_37289)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__36968_37297 = ((G__36971_37287)?(G__36963_37292[(1)]):(cljs.core.truth_(G__36973_37289)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__36969_37298 = ((G__36972_37288)?(G__36964_37293[(0)]):(cljs.core.truth_(G__36974_37290)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__36970_37299 = ((G__36972_37288)?(G__36964_37293[(1)]):(cljs.core.truth_(G__36974_37290)?b:cljs.core.nth.call(null,b,(1),0.0)));
(self__.buf[(0)] = ((G__36965_37294 * G__36967_37296) - G__36969_37298));

(self__.buf[(1)] = ((G__36966_37295 * G__36968_37297) - G__36970_37299));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$abs_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = thi.ng.math.core.abs.call(null,(self__.buf[(0)])));

(self__.buf[(1)] = thi.ng.math.core.abs.call(null,(self__.buf[(1)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$madd_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__36984_37300 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__36985_37301 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__36986_37302 = ((!(G__36984_37300))?typeof a === 'number':null);
var G__36987_37303 = ((!(G__36985_37301))?typeof b === 'number':null);
var G__36975_37304 = self__.buf;
var G__36976_37305 = ((G__36984_37300)?a.buf:null);
var G__36977_37306 = ((G__36985_37301)?b.buf:null);
var G__36978_37307 = (G__36975_37304[(0)]);
var G__36979_37308 = (G__36975_37304[(1)]);
var G__36980_37309 = ((G__36984_37300)?(G__36976_37305[(0)]):(cljs.core.truth_(G__36986_37302)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__36981_37310 = ((G__36984_37300)?(G__36976_37305[(1)]):(cljs.core.truth_(G__36986_37302)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__36982_37311 = ((G__36985_37301)?(G__36977_37306[(0)]):(cljs.core.truth_(G__36987_37303)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__36983_37312 = ((G__36985_37301)?(G__36977_37306[(1)]):(cljs.core.truth_(G__36987_37303)?b:cljs.core.nth.call(null,b,(1),0.0)));
(self__.buf[(0)] = ((G__36978_37307 * G__36980_37309) + G__36982_37311));

(self__.buf[(1)] = ((G__36979_37308 * G__36981_37310) + G__36983_37312));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((1) / (self__.buf[(0)])));

(self__.buf[(1)] = ((1) / (self__.buf[(1)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__36988_37313 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__36989_37314 = v.buf;
(self__.buf[(0)] = ((G__36988_37313[(0)]) / (G__36989_37314[(0)])));

(self__.buf[(1)] = ((G__36988_37313[(1)]) / (G__36989_37314[(1)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__36988_37313[(0)]) / v));

(self__.buf[(1)] = ((G__36988_37313[(1)]) / v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__36988_37313[(0)]) / cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__36988_37313[(1)]) / cljs.core.nth.call(null,v,(1),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__36997_37315 = typeof v1 === 'number';
var G__36998_37316 = typeof v2 === 'number';
if(((G__36997_37315)?G__36998_37316:false)){
(self__.buf[(0)] = ((self__.buf[(0)]) / v1));

(self__.buf[(1)] = ((self__.buf[(1)]) / v2));
} else {
var G__36999_37317 = ((!(G__36997_37315))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37000_37318 = ((!(G__36998_37316))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__36991_37319 = (cljs.core.truth_(G__36999_37317)?v1.buf:null);
var G__36992_37320 = (cljs.core.truth_(G__37000_37318)?v2.buf:null);
var G__36993_37321 = (cljs.core.truth_(G__36999_37317)?(G__36991_37319[(0)]):((G__36997_37315)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__36994_37322 = (cljs.core.truth_(G__36999_37317)?(G__36991_37319[(1)]):((G__36997_37315)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__36995_37323 = (cljs.core.truth_(G__37000_37318)?(G__36992_37320[(0)]):((G__36998_37316)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__36996_37324 = (cljs.core.truth_(G__37000_37318)?(G__36992_37320[(1)]):((G__36998_37316)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(self__.buf[(0)] = (((self__.buf[(0)]) / G__36993_37321) / G__36995_37323));

(self__.buf[(1)] = (((self__.buf[(1)]) / G__36994_37322) / G__36996_37324));
}

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37001_37325 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37002_37326 = v.buf;
(self__.buf[(0)] = ((G__37001_37325[(0)]) + (G__37002_37326[(0)])));

(self__.buf[(1)] = ((G__37001_37325[(1)]) + (G__37002_37326[(1)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__37001_37325[(0)]) + v));

(self__.buf[(1)] = ((G__37001_37325[(1)]) + v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__37001_37325[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__37001_37325[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37010_37327 = typeof v1 === 'number';
var G__37011_37328 = typeof v2 === 'number';
if(((G__37010_37327)?G__37011_37328:false)){
(self__.buf[(0)] = ((self__.buf[(0)]) + v1));

(self__.buf[(1)] = ((self__.buf[(1)]) + v2));
} else {
var G__37012_37329 = ((!(G__37010_37327))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37013_37330 = ((!(G__37011_37328))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37004_37331 = (cljs.core.truth_(G__37012_37329)?v1.buf:null);
var G__37005_37332 = (cljs.core.truth_(G__37013_37330)?v2.buf:null);
var G__37006_37333 = (cljs.core.truth_(G__37012_37329)?(G__37004_37331[(0)]):((G__37010_37327)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37007_37334 = (cljs.core.truth_(G__37012_37329)?(G__37004_37331[(1)]):((G__37010_37327)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37008_37335 = (cljs.core.truth_(G__37013_37330)?(G__37005_37332[(0)]):((G__37011_37328)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37009_37336 = (cljs.core.truth_(G__37013_37330)?(G__37005_37332[(1)]):((G__37011_37328)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(self__.buf[(0)] = (((self__.buf[(0)]) + G__37006_37333) + G__37008_37335));

(self__.buf[(1)] = (((self__.buf[(1)]) + G__37007_37334) + G__37009_37336));
}

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMutableMathOps$addm_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__37023_37337 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__37024_37338 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37025_37339 = ((!(G__37023_37337))?typeof a === 'number':null);
var G__37026_37340 = ((!(G__37024_37338))?typeof b === 'number':null);
var G__37014_37341 = self__.buf;
var G__37015_37342 = ((G__37023_37337)?a.buf:null);
var G__37016_37343 = ((G__37024_37338)?b.buf:null);
var G__37017_37344 = (G__37014_37341[(0)]);
var G__37018_37345 = (G__37014_37341[(1)]);
var G__37019_37346 = ((G__37023_37337)?(G__37015_37342[(0)]):(cljs.core.truth_(G__37025_37339)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37020_37347 = ((G__37023_37337)?(G__37015_37342[(1)]):(cljs.core.truth_(G__37025_37339)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37021_37348 = ((G__37024_37338)?(G__37016_37343[(0)]):(cljs.core.truth_(G__37026_37340)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37022_37349 = ((G__37024_37338)?(G__37016_37343[(1)]):(cljs.core.truth_(G__37026_37340)?b:cljs.core.nth.call(null,b,(1),1.0)));
(self__.buf[(0)] = ((G__37017_37344 + G__37019_37346) * G__37021_37348));

(self__.buf[(1)] = ((G__37018_37345 + G__37020_37347) * G__37022_37349));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IReversible$_rseq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.vector.swizzle2_fns.call(null,new cljs.core.Keyword(null,"yx","yx",1696579752)).call(null,___$1);
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IHash$_hash$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var or__16069__auto__ = self__._hash;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ___$1._hash = cljs.core.mix_collection_hash.call(null,((cljs.core.imul.call(null,(((31) + cljs.core.hash.call(null,(self__.buf[(0)]))) | (0)),(31)) + cljs.core.hash.call(null,(self__.buf[(1)]))) | (0)),(2));
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IEquiv$_equiv$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
if((o instanceof thi.ng.geom.core.vector.Vec2)){
var b_SINGLEQUOTE_ = o.buf;
return (((self__.buf[(0)]) === (b_SINGLEQUOTE_[(0)]))) && (((self__.buf[(1)]) === (b_SINGLEQUOTE_[(1)])));
} else {
return (cljs.core.sequential_QMARK_.call(null,o)) && (((2) === cljs.core.count.call(null,o))) && (cljs.core._EQ_.call(null,(self__.buf[(0)]),cljs.core.first.call(null,o))) && (cljs.core._EQ_.call(null,(self__.buf[(1)]),cljs.core.nth.call(null,o,(1))));
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PHeading$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PHeading$heading$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var t = Math.atan2((self__.buf[(1)]),(self__.buf[(0)]));
if((t < (0))){
return (t + thi.ng.math.core.TWO_PI);
} else {
return t;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PHeading$heading_xy$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.heading.call(null,___$1);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PHeading$angle_between$arity$2 = (function (_,a){
var self__ = this;
var ___$1 = this;
var t = (thi.ng.geom.core.heading.call(null,a) - thi.ng.geom.core.heading.call(null,___$1));
if((t < (0))){
return (t + thi.ng.math.core.TWO_PI);
} else {
return t;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PHeading$slope_xy$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ((self__.buf[(1)]) / (self__.buf[(0)]));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PDistance$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PDistance$dist$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
return Math.sqrt(thi.ng.geom.core.dist_squared.call(null,___$1,v));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PDistance$dist_squared$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37027 = self__.buf;
var G__37029 = (G__37027[(0)]);
var G__37030 = (G__37027[(1)]);
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37028 = v.buf;
var G__37031 = (G__37028[(0)]);
var G__37032 = (G__37028[(1)]);
var dx = (G__37029 - G__37031);
var dy = (G__37030 - G__37032);
return ((dx * dx) + (dy * dy));
} else {
var G__37031 = cljs.core.nth.call(null,v,(0),0.0);
var G__37032 = cljs.core.nth.call(null,v,(1),0.0);
var dx = (G__37029 - G__37031);
var dy = (G__37030 - G__37032);
return ((dx * dx) + (dy * dy));
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IReduce$_reduce$arity$2 = (function (coll,f){
var self__ = this;
var coll__$1 = this;
var acc = f.call(null,(self__.buf[(0)]),(self__.buf[(1)]));
if(cljs.core.reduced_QMARK_.call(null,acc)){
return cljs.core.deref.call(null,acc);
} else {
return acc;
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IReduce$_reduce$arity$3 = (function (coll,f,start){
var self__ = this;
var coll__$1 = this;
var acc = f.call(null,start,(self__.buf[(0)]));
if(cljs.core.reduced_QMARK_.call(null,acc)){
return cljs.core.deref.call(null,acc);
} else {
var acc__$1 = f.call(null,acc,(self__.buf[(1)]));
if(cljs.core.reduced_QMARK_.call(null,acc__$1)){
return cljs.core.deref.call(null,acc__$1);
} else {
return acc__$1;
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInvert$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInvert$invert$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._.call(null,___$1);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$msub$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37042_37350 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__37043_37351 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37044_37352 = ((!(G__37042_37350))?typeof a === 'number':null);
var G__37045_37353 = ((!(G__37043_37351))?typeof b === 'number':null);
var G__37033_37354 = self__.buf;
var G__37034_37355 = ((G__37042_37350)?a.buf:null);
var G__37035_37356 = ((G__37043_37351)?b.buf:null);
var G__37036_37357 = (G__37033_37354[(0)]);
var G__37037_37358 = (G__37033_37354[(1)]);
var G__37038_37359 = ((G__37042_37350)?(G__37034_37355[(0)]):(cljs.core.truth_(G__37044_37352)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37039_37360 = ((G__37042_37350)?(G__37034_37355[(1)]):(cljs.core.truth_(G__37044_37352)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37040_37361 = ((G__37043_37351)?(G__37035_37356[(0)]):(cljs.core.truth_(G__37045_37353)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37041_37362 = ((G__37043_37351)?(G__37035_37356[(1)]):(cljs.core.truth_(G__37045_37353)?b:cljs.core.nth.call(null,b,(1),0.0)));
(dest__18736__auto__[(0)] = ((G__37036_37357 * G__37038_37359) - G__37040_37361));

(dest__18736__auto__[(1)] = ((G__37037_37358 * G__37039_37360) - G__37041_37362));

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37046_37363 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37047_37364 = v.buf;
(dest__18725__auto__[(0)] = ((G__37046_37363[(0)]) * (G__37047_37364[(0)])));

(dest__18725__auto__[(1)] = ((G__37046_37363[(1)]) * (G__37047_37364[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37046_37363[(0)]) * v));

(dest__18725__auto__[(1)] = ((G__37046_37363[(1)]) * v));
} else {
(dest__18725__auto__[(0)] = ((G__37046_37363[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37046_37363[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37048 = self__.buf;
var G__37051 = (new Float32Array((2)));
var G__37052 = (G__37048[(0)]);
var G__37053 = (G__37048[(1)]);
var G__37058 = typeof v1 === 'number';
var G__37059 = typeof v2 === 'number';
if(((G__37058)?G__37059:false)){
(G__37051[(0)] = (G__37052 * v1));

(G__37051[(1)] = (G__37053 * v2));
} else {
var G__37060_37365 = ((!(G__37058))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37061_37366 = ((!(G__37059))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37049_37367 = (cljs.core.truth_(G__37060_37365)?v1.buf:null);
var G__37050_37368 = (cljs.core.truth_(G__37061_37366)?v2.buf:null);
var G__37054_37369 = (cljs.core.truth_(G__37060_37365)?(G__37049_37367[(0)]):((G__37058)?v1:cljs.core.nth.call(null,v1,(0),1.0)));
var G__37055_37370 = (cljs.core.truth_(G__37060_37365)?(G__37049_37367[(1)]):((G__37058)?v1:cljs.core.nth.call(null,v1,(1),1.0)));
var G__37056_37371 = (cljs.core.truth_(G__37061_37366)?(G__37050_37368[(0)]):((G__37059)?v2:cljs.core.nth.call(null,v2,(0),1.0)));
var G__37057_37372 = (cljs.core.truth_(G__37061_37366)?(G__37050_37368[(1)]):((G__37059)?v2:cljs.core.nth.call(null,v2,(1),1.0)));
(G__37051[(0)] = ((G__37052 * G__37054_37369) * G__37056_37371));

(G__37051[(1)] = ((G__37053 * G__37055_37370) * G__37057_37372));
}

return (new thi.ng.geom.core.vector.Vec2(G__37051,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18699__auto__ = (new Float32Array((2)));
var G__37062_37373 = self__.buf;
(dest__18699__auto__[(0)] = (- (G__37062_37373[(0)])));

(dest__18699__auto__[(1)] = (- (G__37062_37373[(1)])));

return (new thi.ng.geom.core.vector.Vec2(dest__18699__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37063_37374 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37064_37375 = v.buf;
(dest__18725__auto__[(0)] = ((G__37063_37374[(0)]) - (G__37064_37375[(0)])));

(dest__18725__auto__[(1)] = ((G__37063_37374[(1)]) - (G__37064_37375[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37063_37374[(0)]) - v));

(dest__18725__auto__[(1)] = ((G__37063_37374[(1)]) - v));
} else {
(dest__18725__auto__[(0)] = ((G__37063_37374[(0)]) - cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37063_37374[(1)]) - cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37065 = self__.buf;
var G__37068 = (new Float32Array((2)));
var G__37069 = (G__37065[(0)]);
var G__37070 = (G__37065[(1)]);
var G__37075 = typeof v1 === 'number';
var G__37076 = typeof v2 === 'number';
if(((G__37075)?G__37076:false)){
(G__37068[(0)] = (G__37069 - v1));

(G__37068[(1)] = (G__37070 - v2));
} else {
var G__37077_37376 = ((!(G__37075))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37078_37377 = ((!(G__37076))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37066_37378 = (cljs.core.truth_(G__37077_37376)?v1.buf:null);
var G__37067_37379 = (cljs.core.truth_(G__37078_37377)?v2.buf:null);
var G__37071_37380 = (cljs.core.truth_(G__37077_37376)?(G__37066_37378[(0)]):((G__37075)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37072_37381 = (cljs.core.truth_(G__37077_37376)?(G__37066_37378[(1)]):((G__37075)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37073_37382 = (cljs.core.truth_(G__37078_37377)?(G__37067_37379[(0)]):((G__37076)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37074_37383 = (cljs.core.truth_(G__37078_37377)?(G__37067_37379[(1)]):((G__37076)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(G__37068[(0)] = ((G__37069 - G__37071_37380) - G__37073_37382));

(G__37068[(1)] = ((G__37070 - G__37072_37381) - G__37074_37383));
}

return (new thi.ng.geom.core.vector.Vec2(G__37068,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$madd$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37088_37384 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__37089_37385 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37090_37386 = ((!(G__37088_37384))?typeof a === 'number':null);
var G__37091_37387 = ((!(G__37089_37385))?typeof b === 'number':null);
var G__37079_37388 = self__.buf;
var G__37080_37389 = ((G__37088_37384)?a.buf:null);
var G__37081_37390 = ((G__37089_37385)?b.buf:null);
var G__37082_37391 = (G__37079_37388[(0)]);
var G__37083_37392 = (G__37079_37388[(1)]);
var G__37084_37393 = ((G__37088_37384)?(G__37080_37389[(0)]):(cljs.core.truth_(G__37090_37386)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37085_37394 = ((G__37088_37384)?(G__37080_37389[(1)]):(cljs.core.truth_(G__37090_37386)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37086_37395 = ((G__37089_37385)?(G__37081_37390[(0)]):(cljs.core.truth_(G__37091_37387)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37087_37396 = ((G__37089_37385)?(G__37081_37390[(1)]):(cljs.core.truth_(G__37091_37387)?b:cljs.core.nth.call(null,b,(1),0.0)));
(dest__18736__auto__[(0)] = ((G__37082_37391 * G__37084_37393) + G__37086_37395));

(dest__18736__auto__[(1)] = ((G__37083_37392 * G__37085_37394) + G__37087_37396));

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$addm$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37101_37397 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__37102_37398 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37103_37399 = ((!(G__37101_37397))?typeof a === 'number':null);
var G__37104_37400 = ((!(G__37102_37398))?typeof b === 'number':null);
var G__37092_37401 = self__.buf;
var G__37093_37402 = ((G__37101_37397)?a.buf:null);
var G__37094_37403 = ((G__37102_37398)?b.buf:null);
var G__37095_37404 = (G__37092_37401[(0)]);
var G__37096_37405 = (G__37092_37401[(1)]);
var G__37097_37406 = ((G__37101_37397)?(G__37093_37402[(0)]):(cljs.core.truth_(G__37103_37399)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37098_37407 = ((G__37101_37397)?(G__37093_37402[(1)]):(cljs.core.truth_(G__37103_37399)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37099_37408 = ((G__37102_37398)?(G__37094_37403[(0)]):(cljs.core.truth_(G__37104_37400)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37100_37409 = ((G__37102_37398)?(G__37094_37403[(1)]):(cljs.core.truth_(G__37104_37400)?b:cljs.core.nth.call(null,b,(1),1.0)));
(dest__18736__auto__[(0)] = ((G__37095_37404 + G__37097_37406) * G__37099_37408));

(dest__18736__auto__[(1)] = ((G__37096_37405 + G__37098_37407) * G__37100_37409));

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$div$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18699__auto__ = (new Float32Array((2)));
var G__37105_37410 = self__.buf;
(dest__18699__auto__[(0)] = ((1) / (G__37105_37410[(0)])));

(dest__18699__auto__[(1)] = ((1) / (G__37105_37410[(1)])));

return (new thi.ng.geom.core.vector.Vec2(dest__18699__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$div$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37106_37411 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37107_37412 = v.buf;
(dest__18725__auto__[(0)] = ((G__37106_37411[(0)]) / (G__37107_37412[(0)])));

(dest__18725__auto__[(1)] = ((G__37106_37411[(1)]) / (G__37107_37412[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37106_37411[(0)]) / v));

(dest__18725__auto__[(1)] = ((G__37106_37411[(1)]) / v));
} else {
(dest__18725__auto__[(0)] = ((G__37106_37411[(0)]) / cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37106_37411[(1)]) / cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$div$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37108 = self__.buf;
var G__37111 = (new Float32Array((2)));
var G__37112 = (G__37108[(0)]);
var G__37113 = (G__37108[(1)]);
var G__37118 = typeof v1 === 'number';
var G__37119 = typeof v2 === 'number';
if(((G__37118)?G__37119:false)){
(G__37111[(0)] = (G__37112 / v1));

(G__37111[(1)] = (G__37113 / v2));
} else {
var G__37120_37413 = ((!(G__37118))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37121_37414 = ((!(G__37119))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37109_37415 = (cljs.core.truth_(G__37120_37413)?v1.buf:null);
var G__37110_37416 = (cljs.core.truth_(G__37121_37414)?v2.buf:null);
var G__37114_37417 = (cljs.core.truth_(G__37120_37413)?(G__37109_37415[(0)]):((G__37118)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37115_37418 = (cljs.core.truth_(G__37120_37413)?(G__37109_37415[(1)]):((G__37118)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37116_37419 = (cljs.core.truth_(G__37121_37414)?(G__37110_37416[(0)]):((G__37119)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37117_37420 = (cljs.core.truth_(G__37121_37414)?(G__37110_37416[(1)]):((G__37119)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(G__37111[(0)] = ((G__37112 / G__37114_37417) / G__37116_37419));

(G__37111[(1)] = ((G__37113 / G__37115_37418) / G__37117_37420));
}

return (new thi.ng.geom.core.vector.Vec2(G__37111,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37122_37421 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37123_37422 = v.buf;
(dest__18725__auto__[(0)] = ((G__37122_37421[(0)]) + (G__37123_37422[(0)])));

(dest__18725__auto__[(1)] = ((G__37122_37421[(1)]) + (G__37123_37422[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37122_37421[(0)]) + v));

(dest__18725__auto__[(1)] = ((G__37122_37421[(1)]) + v));
} else {
(dest__18725__auto__[(0)] = ((G__37122_37421[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37122_37421[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37124 = self__.buf;
var G__37127 = (new Float32Array((2)));
var G__37128 = (G__37124[(0)]);
var G__37129 = (G__37124[(1)]);
var G__37134 = typeof v1 === 'number';
var G__37135 = typeof v2 === 'number';
if(((G__37134)?G__37135:false)){
(G__37127[(0)] = (G__37128 + v1));

(G__37127[(1)] = (G__37129 + v2));
} else {
var G__37136_37423 = ((!(G__37134))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37137_37424 = ((!(G__37135))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37125_37425 = (cljs.core.truth_(G__37136_37423)?v1.buf:null);
var G__37126_37426 = (cljs.core.truth_(G__37137_37424)?v2.buf:null);
var G__37130_37427 = (cljs.core.truth_(G__37136_37423)?(G__37125_37425[(0)]):((G__37134)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37131_37428 = (cljs.core.truth_(G__37136_37423)?(G__37125_37425[(1)]):((G__37134)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37132_37429 = (cljs.core.truth_(G__37137_37424)?(G__37126_37426[(0)]):((G__37135)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37133_37430 = (cljs.core.truth_(G__37137_37424)?(G__37126_37426[(1)]):((G__37135)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(G__37127[(0)] = ((G__37128 + G__37130_37427) + G__37132_37429));

(G__37127[(1)] = ((G__37129 + G__37131_37428) + G__37133_37430));
}

return (new thi.ng.geom.core.vector.Vec2(G__37127,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$abs$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18699__auto__ = (new Float32Array((2)));
var G__37138_37431 = self__.buf;
(dest__18699__auto__[(0)] = thi.ng.math.core.abs.call(null,(G__37138_37431[(0)])));

(dest__18699__auto__[(1)] = thi.ng.math.core.abs.call(null,(G__37138_37431[(1)])));

return (new thi.ng.geom.core.vector.Vec2(dest__18699__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMathOps$subm$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37148_37432 = (a instanceof thi.ng.geom.core.vector.Vec2);
var G__37149_37433 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37150_37434 = ((!(G__37148_37432))?typeof a === 'number':null);
var G__37151_37435 = ((!(G__37149_37433))?typeof b === 'number':null);
var G__37139_37436 = self__.buf;
var G__37140_37437 = ((G__37148_37432)?a.buf:null);
var G__37141_37438 = ((G__37149_37433)?b.buf:null);
var G__37142_37439 = (G__37139_37436[(0)]);
var G__37143_37440 = (G__37139_37436[(1)]);
var G__37144_37441 = ((G__37148_37432)?(G__37140_37437[(0)]):(cljs.core.truth_(G__37150_37434)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37145_37442 = ((G__37148_37432)?(G__37140_37437[(1)]):(cljs.core.truth_(G__37150_37434)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37146_37443 = ((G__37149_37433)?(G__37141_37438[(0)]):(cljs.core.truth_(G__37151_37435)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37147_37444 = ((G__37149_37433)?(G__37141_37438[(1)]):(cljs.core.truth_(G__37151_37435)?b:cljs.core.nth.call(null,b,(1),1.0)));
(dest__18736__auto__[(0)] = ((G__37142_37439 - G__37144_37441) * G__37146_37443));

(dest__18736__auto__[(1)] = ((G__37143_37440 - G__37145_37442) * G__37147_37444));

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormal$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PNormal$normal$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
(b[(0)] = (- (self__.buf[(1)])));

(b[(1)] = (self__.buf[(0)]));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ISeq$_first$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (self__.buf[(0)]);
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ISeq$_rest$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.cons.call(null,(self__.buf[(1)]),null);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PTranslate$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PTranslate$translate$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37152_37445 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37153_37446 = v.buf;
(dest__18725__auto__[(0)] = ((G__37152_37445[(0)]) + (G__37153_37446[(0)])));

(dest__18725__auto__[(1)] = ((G__37152_37445[(1)]) + (G__37153_37446[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37152_37445[(0)]) + v));

(dest__18725__auto__[(1)] = ((G__37152_37445[(1)]) + v));
} else {
(dest__18725__auto__[(0)] = ((G__37152_37445[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37152_37445[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PTranslate$translate$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37154 = self__.buf;
var G__37157 = (new Float32Array((2)));
var G__37158 = (G__37154[(0)]);
var G__37159 = (G__37154[(1)]);
var G__37164 = typeof v1 === 'number';
var G__37165 = typeof v2 === 'number';
if(((G__37164)?G__37165:false)){
(G__37157[(0)] = (G__37158 + v1));

(G__37157[(1)] = (G__37159 + v2));
} else {
var G__37166_37447 = ((!(G__37164))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37167_37448 = ((!(G__37165))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37155_37449 = (cljs.core.truth_(G__37166_37447)?v1.buf:null);
var G__37156_37450 = (cljs.core.truth_(G__37167_37448)?v2.buf:null);
var G__37160_37451 = (cljs.core.truth_(G__37166_37447)?(G__37155_37449[(0)]):((G__37164)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37161_37452 = (cljs.core.truth_(G__37166_37447)?(G__37155_37449[(1)]):((G__37164)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37162_37453 = (cljs.core.truth_(G__37167_37448)?(G__37156_37450[(0)]):((G__37165)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37163_37454 = (cljs.core.truth_(G__37167_37448)?(G__37156_37450[(1)]):((G__37165)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(G__37157[(0)] = ((G__37158 + G__37160_37451) + G__37162_37453));

(G__37157[(1)] = ((G__37159 + G__37161_37452) + G__37163_37454));
}

return (new thi.ng.geom.core.vector.Vec2(G__37157,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IAssociative$_contains_key_QMARK_$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
if(typeof k === 'number'){
return ((k >= (0))) && ((k < (2)));
} else {
if(cljs.core.truth_(thi.ng.geom.core.vector.swizzle2_fns.call(null,k))){
return true;
} else {
return false;
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IAssociative$_assoc$arity$3 = (function (_,k,v){
var self__ = this;
var ___$1 = this;
if(typeof k === 'number'){
if(((k === (0))) || ((k === (1)))){
var b = (new Float32Array(self__.buf));
(b[k] = v);

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
} else {
if((k === (2))){
return cljs.core.conj.call(null,___$1,v);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
} else {
if((k instanceof cljs.core.Keyword)){
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"z","z",-789527183),k)){
return cljs.core.conj.call(null,___$1,v);
} else {
return (new thi.ng.geom.core.vector.Vec2(thi.ng.geom.core.vector.swizzle_assoc_STAR_.call(null,self__.buf,(new Float32Array(self__.buf)),new cljs.core.PersistentArrayMap(null, 2, ["x",(0),"y",(1)], null),k,v),null,self__._meta));
}
} else {
return null;
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ISeqable$_seq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PScale$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PScale$scale$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37168_37455 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37169_37456 = v.buf;
(dest__18725__auto__[(0)] = ((G__37168_37455[(0)]) * (G__37169_37456[(0)])));

(dest__18725__auto__[(1)] = ((G__37168_37455[(1)]) * (G__37169_37456[(1)])));
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = ((G__37168_37455[(0)]) * v));

(dest__18725__auto__[(1)] = ((G__37168_37455[(1)]) * v));
} else {
(dest__18725__auto__[(0)] = ((G__37168_37455[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(dest__18725__auto__[(1)] = ((G__37168_37455[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PScale$scale$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37170 = self__.buf;
var G__37173 = (new Float32Array((2)));
var G__37174 = (G__37170[(0)]);
var G__37175 = (G__37170[(1)]);
var G__37180 = typeof v1 === 'number';
var G__37181 = typeof v2 === 'number';
if(((G__37180)?G__37181:false)){
(G__37173[(0)] = (G__37174 * v1));

(G__37173[(1)] = (G__37175 * v2));
} else {
var G__37182_37457 = ((!(G__37180))?(v1 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37183_37458 = ((!(G__37181))?(v2 instanceof thi.ng.geom.core.vector.Vec2):null);
var G__37171_37459 = (cljs.core.truth_(G__37182_37457)?v1.buf:null);
var G__37172_37460 = (cljs.core.truth_(G__37183_37458)?v2.buf:null);
var G__37176_37461 = (cljs.core.truth_(G__37182_37457)?(G__37171_37459[(0)]):((G__37180)?v1:cljs.core.nth.call(null,v1,(0),1.0)));
var G__37177_37462 = (cljs.core.truth_(G__37182_37457)?(G__37171_37459[(1)]):((G__37180)?v1:cljs.core.nth.call(null,v1,(1),1.0)));
var G__37178_37463 = (cljs.core.truth_(G__37183_37458)?(G__37172_37460[(0)]):((G__37181)?v2:cljs.core.nth.call(null,v2,(0),1.0)));
var G__37179_37464 = (cljs.core.truth_(G__37183_37458)?(G__37172_37460[(1)]):((G__37181)?v2:cljs.core.nth.call(null,v2,(1),1.0)));
(G__37173[(0)] = ((G__37174 * G__37176_37461) * G__37178_37463));

(G__37173[(1)] = ((G__37175 * G__37177_37462) * G__37179_37464));
}

return (new thi.ng.geom.core.vector.Vec2(G__37173,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec2((new Float32Array(self__.buf)),self__._hash,m));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$ICollection$_conj$arity$2 = (function (_,x){
var self__ = this;
var ___$1 = this;
return cljs.core.with_meta.call(null,thi.ng.geom.core.vector.vec3.call(null,(self__.buf[(0)]),(self__.buf[(1)]),x),self__._meta);
});

thi.ng.geom.core.vector.Vec2.prototype.call = (function() {
var G__37465 = null;
var G__37465__2 = (function (self__,k){
var self__ = this;
var self____$1 = this;
var _ = self____$1;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});
var G__37465__3 = (function (self__,k,nf){
var self__ = this;
var self____$1 = this;
var _ = self____$1;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return nf;
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return nf;
}
}
});
G__37465 = function(self__,k,nf){
switch(arguments.length){
case 2:
return G__37465__2.call(this,self__,k);
case 3:
return G__37465__3.call(this,self__,k,nf);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
G__37465.cljs$core$IFn$_invoke$arity$2 = G__37465__2;
G__37465.cljs$core$IFn$_invoke$arity$3 = G__37465__3;
return G__37465;
})()
;

thi.ng.geom.core.vector.Vec2.prototype.apply = (function (self__,args36900){
var self__ = this;
var self____$1 = this;
return self____$1.call.apply(self____$1,[self____$1].concat(cljs.core.aclone.call(null,args36900)));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IFn$_invoke$arity$1 = (function (k){
var self__ = this;
var _ = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IFn$_invoke$arity$2 = (function (k,nf){
var self__ = this;
var _ = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle2_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return nf;
}
} else {
if(((k >= (0))) && ((k < (2)))){
return (self__.buf[k]);
} else {
return nf;
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMinMax$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMinMax$min$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37184_37466 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37185_37467 = v.buf;
(dest__18725__auto__[(0)] = (function (){var a__18454__auto__ = (G__37184_37466[(0)]);
var b__18455__auto__ = (G__37185_37467[(0)]);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18454__auto__ = (G__37184_37466[(1)]);
var b__18455__auto__ = (G__37185_37467[(1)]);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = (function (){var a__18454__auto__ = (G__37184_37466[(0)]);
var b__18455__auto__ = v;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18454__auto__ = (G__37184_37466[(1)]);
var b__18455__auto__ = v;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
} else {
(dest__18725__auto__[(0)] = (function (){var a__18454__auto__ = (G__37184_37466[(0)]);
var b__18455__auto__ = cljs.core.nth.call(null,v,(0),0.0);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18454__auto__ = (G__37184_37466[(1)]);
var b__18455__auto__ = cljs.core.nth.call(null,v,(1),0.0);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMinMax$min$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37195_37468 = (v instanceof thi.ng.geom.core.vector.Vec2);
var G__37196_37469 = (v2 instanceof thi.ng.geom.core.vector.Vec2);
var G__37197_37470 = ((!(G__37195_37468))?typeof v === 'number':null);
var G__37198_37471 = ((!(G__37196_37469))?typeof v2 === 'number':null);
var G__37186_37472 = self__.buf;
var G__37187_37473 = ((G__37195_37468)?v.buf:null);
var G__37188_37474 = ((G__37196_37469)?v2.buf:null);
var G__37189_37475 = (G__37186_37472[(0)]);
var G__37190_37476 = (G__37186_37472[(1)]);
var G__37191_37477 = ((G__37195_37468)?(G__37187_37473[(0)]):(cljs.core.truth_(G__37197_37470)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37192_37478 = ((G__37195_37468)?(G__37187_37473[(1)]):(cljs.core.truth_(G__37197_37470)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37193_37479 = ((G__37196_37469)?(G__37188_37474[(0)]):(cljs.core.truth_(G__37198_37471)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37194_37480 = ((G__37196_37469)?(G__37188_37474[(1)]):(cljs.core.truth_(G__37198_37471)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(dest__18736__auto__[(0)] = (function (){var a__18454__auto__ = (function (){var a__18454__auto__ = G__37189_37475;
var b__18455__auto__ = G__37191_37477;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})();
var b__18455__auto__ = G__37193_37479;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18736__auto__[(1)] = (function (){var a__18454__auto__ = (function (){var a__18454__auto__ = G__37190_37476;
var b__18455__auto__ = G__37192_37478;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})();
var b__18455__auto__ = G__37194_37480;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMinMax$max$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18725__auto__ = (new Float32Array((2)));
var G__37199_37481 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37200_37482 = v.buf;
(dest__18725__auto__[(0)] = (function (){var a__18461__auto__ = (G__37199_37481[(0)]);
var b__18462__auto__ = (G__37200_37482[(0)]);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18461__auto__ = (G__37199_37481[(1)]);
var b__18462__auto__ = (G__37200_37482[(1)]);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
} else {
if(typeof v === 'number'){
(dest__18725__auto__[(0)] = (function (){var a__18461__auto__ = (G__37199_37481[(0)]);
var b__18462__auto__ = v;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18461__auto__ = (G__37199_37481[(1)]);
var b__18462__auto__ = v;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
} else {
(dest__18725__auto__[(0)] = (function (){var a__18461__auto__ = (G__37199_37481[(0)]);
var b__18462__auto__ = cljs.core.nth.call(null,v,(0),0.0);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18725__auto__[(1)] = (function (){var a__18461__auto__ = (G__37199_37481[(1)]);
var b__18462__auto__ = cljs.core.nth.call(null,v,(1),0.0);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
}
}

return (new thi.ng.geom.core.vector.Vec2(dest__18725__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMinMax$max$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var dest__18736__auto__ = (new Float32Array((2)));
var G__37210_37483 = (v instanceof thi.ng.geom.core.vector.Vec2);
var G__37211_37484 = (v2 instanceof thi.ng.geom.core.vector.Vec2);
var G__37212_37485 = ((!(G__37210_37483))?typeof v === 'number':null);
var G__37213_37486 = ((!(G__37211_37484))?typeof v2 === 'number':null);
var G__37201_37487 = self__.buf;
var G__37202_37488 = ((G__37210_37483)?v.buf:null);
var G__37203_37489 = ((G__37211_37484)?v2.buf:null);
var G__37204_37490 = (G__37201_37487[(0)]);
var G__37205_37491 = (G__37201_37487[(1)]);
var G__37206_37492 = ((G__37210_37483)?(G__37202_37488[(0)]):(cljs.core.truth_(G__37212_37485)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37207_37493 = ((G__37210_37483)?(G__37202_37488[(1)]):(cljs.core.truth_(G__37212_37485)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37208_37494 = ((G__37211_37484)?(G__37203_37489[(0)]):(cljs.core.truth_(G__37213_37486)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37209_37495 = ((G__37211_37484)?(G__37203_37489[(1)]):(cljs.core.truth_(G__37213_37486)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(dest__18736__auto__[(0)] = (function (){var a__18461__auto__ = (function (){var a__18461__auto__ = G__37204_37490;
var b__18462__auto__ = G__37206_37492;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})();
var b__18462__auto__ = G__37208_37494;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18736__auto__[(1)] = (function (){var a__18461__auto__ = (function (){var a__18461__auto__ = G__37205_37491;
var b__18462__auto__ = G__37207_37493;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})();
var b__18462__auto__ = G__37209_37495;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

return (new thi.ng.geom.core.vector.Vec2(dest__18736__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.cljs$core$IComparable$_compare$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
if((o instanceof thi.ng.geom.core.vector.Vec2)){
var b_SINGLEQUOTE_ = o.buf;
var c = cljs.core.compare.call(null,(self__.buf[(0)]),(b_SINGLEQUOTE_[(0)]));
if(((0) === c)){
return cljs.core.compare.call(null,(self__.buf[(1)]),(b_SINGLEQUOTE_[(1)]));
} else {
return c;
}
} else {
var c = cljs.core.count.call(null,o);
if(((2) === c)){
return (- cljs.core.compare.call(null,o,___$1));
} else {
return ((2) - c);
}
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PVectorReduce$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PVectorReduce$reduce_vector$arity$3 = (function (_,f,xs){
var self__ = this;
var ___$1 = this;
var buf_SINGLEQUOTE_ = (new Float32Array(self__.buf));
return (new thi.ng.geom.core.vector.Vec2(thi.ng.geom.core.vector.vec2_reduce_STAR_.call(null,f,buf_SINGLEQUOTE_,xs),null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PVectorReduce$reduce_vector$arity$4 = (function (_,f,f2,xs){
var self__ = this;
var ___$1 = this;
var buf_SINGLEQUOTE_ = (new Float32Array(self__.buf));
thi.ng.geom.core.vector.vec2_reduce_STAR_.call(null,f,buf_SINGLEQUOTE_,xs);

(buf_SINGLEQUOTE_[(0)] = f2.call(null,(buf_SINGLEQUOTE_[(0)]),(0)));

(buf_SINGLEQUOTE_[(1)] = f2.call(null,(buf_SINGLEQUOTE_[(1)]),(1)));

return (new thi.ng.geom.core.vector.Vec2(buf_SINGLEQUOTE_,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$math$core$PDeltaEquals$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,___$1,v,thi.ng.math.core._STAR_eps_STAR_);
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$3 = (function (_,v,eps){
var self__ = this;
var ___$1 = this;
if(cljs.core.sequential_QMARK_.call(null,v)){
if(((2) === cljs.core.count.call(null,v))){
var G__37214 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37215 = v.buf;
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37214[(0)]),(G__37215[(0)]),eps))){
return thi.ng.math.core.delta_EQ_.call(null,(G__37214[(1)]),(G__37215[(1)]),eps);
} else {
return null;
}
} else {
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37214[(0)]),cljs.core.nth.call(null,v,(0),0.0),eps))){
return thi.ng.math.core.delta_EQ_.call(null,(G__37214[(1)]),cljs.core.nth.call(null,v,(1),0.0),eps);
} else {
return null;
}
}
} else {
return null;
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMagnitude$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMagnitude$mag$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__37216 = self__.buf;
var G__37217 = (G__37216[(0)]);
var G__37218 = (G__37216[(1)]);
return Math.sqrt(((G__37217 * G__37217) + (G__37218 * G__37218)));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PMagnitude$mag_squared$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__37219 = self__.buf;
var G__37220 = (G__37219[(0)]);
var G__37221 = (G__37219[(1)]);
return ((G__37220 * G__37220) + (G__37221 * G__37221));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInterpolate$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInterpolate$mix$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
var G__37222_37496 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec2)){
var G__37223_37497 = v.buf;
(b[(0)] = (((G__37222_37496[(0)]) + (G__37223_37497[(0)])) * 0.5));

(b[(1)] = (((G__37222_37496[(1)]) + (G__37223_37497[(1)])) * 0.5));
} else {
if(typeof v === 'number'){
(b[(0)] = (((G__37222_37496[(0)]) + v) * 0.5));

(b[(1)] = (((G__37222_37496[(1)]) + v) * 0.5));
} else {
(b[(0)] = (((G__37222_37496[(0)]) + cljs.core.nth.call(null,v,(0),0.0)) * 0.5));

(b[(1)] = (((G__37222_37496[(1)]) + cljs.core.nth.call(null,v,(1),0.0)) * 0.5));
}
}

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInterpolate$mix$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
var G__37233_37498 = (v instanceof thi.ng.geom.core.vector.Vec2);
var G__37234_37499 = (v2 instanceof thi.ng.geom.core.vector.Vec2);
var G__37235_37500 = ((!(G__37233_37498))?typeof v === 'number':null);
var G__37236_37501 = ((!(G__37234_37499))?typeof v2 === 'number':null);
var G__37224_37502 = self__.buf;
var G__37225_37503 = ((G__37233_37498)?v.buf:null);
var G__37226_37504 = ((G__37234_37499)?v2.buf:null);
var G__37227_37505 = (G__37224_37502[(0)]);
var G__37228_37506 = (G__37224_37502[(1)]);
var G__37229_37507 = ((G__37233_37498)?(G__37225_37503[(0)]):(cljs.core.truth_(G__37235_37500)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37230_37508 = ((G__37233_37498)?(G__37225_37503[(1)]):(cljs.core.truth_(G__37235_37500)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37231_37509 = ((G__37234_37499)?(G__37226_37504[(0)]):(cljs.core.truth_(G__37236_37501)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37232_37510 = ((G__37234_37499)?(G__37226_37504[(1)]):(cljs.core.truth_(G__37236_37501)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
(b[(0)] = (((G__37229_37507 - G__37227_37505) * G__37231_37509) + G__37227_37505));

(b[(1)] = (((G__37230_37508 - G__37228_37506) * G__37232_37510) + G__37228_37506));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PInterpolate$mix$arity$6 = (function (_,b,c,d,u,v){
var self__ = this;
var ___$1 = this;
var b_SINGLEQUOTE_ = (new Float32Array((2)));
var dv_QMARK_ = (d instanceof thi.ng.geom.core.vector.Vec2);
var dn_QMARK_ = typeof d === 'number';
var dv = ((dv_QMARK_)?d.buf:null);
var dx = ((dv_QMARK_)?(dv[(0)]):((dn_QMARK_)?d:cljs.core.nth.call(null,d,(0),0.0)));
var dy = ((dv_QMARK_)?(dv[(1)]):((dn_QMARK_)?d:cljs.core.nth.call(null,d,(1),0.0)));
var G__37246_37511 = (b instanceof thi.ng.geom.core.vector.Vec2);
var G__37247_37512 = (c instanceof thi.ng.geom.core.vector.Vec2);
var G__37248_37513 = ((!(G__37246_37511))?typeof b === 'number':null);
var G__37249_37514 = ((!(G__37247_37512))?typeof c === 'number':null);
var G__37237_37515 = self__.buf;
var G__37238_37516 = ((G__37246_37511)?b.buf:null);
var G__37239_37517 = ((G__37247_37512)?c.buf:null);
var G__37240_37518 = (G__37237_37515[(0)]);
var G__37241_37519 = (G__37237_37515[(1)]);
var G__37242_37520 = ((G__37246_37511)?(G__37238_37516[(0)]):(cljs.core.truth_(G__37248_37513)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37243_37521 = ((G__37246_37511)?(G__37238_37516[(1)]):(cljs.core.truth_(G__37248_37513)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37244_37522 = ((G__37247_37512)?(G__37239_37517[(0)]):(cljs.core.truth_(G__37249_37514)?c:cljs.core.nth.call(null,c,(0),0.0)));
var G__37245_37523 = ((G__37247_37512)?(G__37239_37517[(1)]):(cljs.core.truth_(G__37249_37514)?c:cljs.core.nth.call(null,c,(1),0.0)));
var x1_37524 = (((G__37242_37520 - G__37240_37518) * u) + G__37240_37518);
var y1_37525 = (((G__37243_37521 - G__37241_37519) * u) + G__37241_37519);
(b_SINGLEQUOTE_[(0)] = ((((((dx - G__37244_37522) * u) + G__37244_37522) - x1_37524) * v) + x1_37524));

(b_SINGLEQUOTE_[(1)] = ((((((dy - G__37245_37523) * u) + G__37245_37523) - y1_37525) * v) + y1_37525));

return (new thi.ng.geom.core.vector.Vec2(b_SINGLEQUOTE_,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PLimit$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PLimit$limit$arity$2 = (function (_,len){
var self__ = this;
var ___$1 = this;
if((thi.ng.geom.core.mag_squared.call(null,___$1) > (len * len))){
return thi.ng.geom.core.normalize.call(null,___$1,len);
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PPolar$ = true;

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PPolar$as_polar$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
(b[(0)] = thi.ng.geom.core.mag.call(null,___$1));

(b[(1)] = thi.ng.geom.core.heading.call(null,___$1));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.prototype.thi$ng$geom$core$PPolar$as_cartesian$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__36920 = self__.buf;
var G__36921 = (G__36920[(0)]);
var G__36922 = (G__36920[(1)]);
var b = (new Float32Array((2)));
(b[(0)] = (G__36921 * Math.cos(G__36922)));

(b[(1)] = (G__36921 * Math.sin(G__36922)));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec2.getBasis = (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"buf","buf",1426618187,null),new cljs.core.Symbol(null,"_hash","_hash",-2130838312,null),new cljs.core.Symbol(null,"_meta","_meta",-1716892533,null)], null);
});

thi.ng.geom.core.vector.Vec2.cljs$lang$type = true;

thi.ng.geom.core.vector.Vec2.cljs$lang$ctorStr = "thi.ng.geom.core.vector/Vec2";

thi.ng.geom.core.vector.Vec2.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"thi.ng.geom.core.vector/Vec2");
});

thi.ng.geom.core.vector.__GT_Vec2 = (function thi$ng$geom$core$vector$__GT_Vec2(buf,_hash,_meta){
return (new thi.ng.geom.core.vector.Vec2(buf,_hash,_meta));
});


/**
* @constructor
*/
thi.ng.geom.core.vector.Vec3 = (function (buf,_hash,_meta){
this.buf = buf;
this._hash = _hash;
this._meta = _meta;
this.cljs$lang$protocol_mask$partition0$ = 166618075;
this.cljs$lang$protocol_mask$partition1$ = 10240;
})
thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PBuffered$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PBuffered$get_buffer$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.buf;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PBuffered$copy_to_buffer$arity$4 = (function (_,dest,stride,idx){
var self__ = this;
var ___$1 = this;
dest.set(self__.buf,idx);

return (idx + stride);
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTransform$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTransform$transform$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.transform_vector.call(null,m,___$1);
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate$rotate$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.rotate_z.call(null,___$1,theta);
});

thi.ng.geom.core.vector.Vec3.prototype.toString = (function (){
var self__ = this;
var _ = this;
return [cljs.core.str("["),cljs.core.str((self__.buf[(0)])),cljs.core.str(" "),cljs.core.str((self__.buf[(1)])),cljs.core.str(" "),cljs.core.str((self__.buf[(2)])),cljs.core.str("]")].join('');
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ILookup$_lookup$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,___$1);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ILookup$_lookup$arity$3 = (function (_,k,nf){
var self__ = this;
var ___$1 = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,___$1);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PDotProduct$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PDotProduct$dot$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37527 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37528 = v.buf;
return ((((G__37527[(0)]) * (G__37528[(0)])) + ((G__37527[(1)]) * (G__37528[(1)]))) + ((G__37527[(2)]) * (G__37528[(2)])));
} else {
return ((((G__37527[(0)]) * cljs.core.nth.call(null,v,(0),0.0)) + ((G__37527[(1)]) * cljs.core.nth.call(null,v,(1),0.0))) + ((G__37527[(2)]) * cljs.core.nth.call(null,v,(2),0.0)));
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PNormalize$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PNormalize$normalize$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__37529 = self__.buf;
var G__37530 = (G__37529[(0)]);
var G__37531 = (G__37529[(1)]);
var G__37532 = (G__37529[(2)]);
var l = Math.sqrt((((G__37530 * G__37530) + (G__37531 * G__37531)) + (G__37532 * G__37532)));
if((l > (0))){
var b = (new Float32Array((3)));
(b[(0)] = (G__37530 / l));

(b[(1)] = (G__37531 / l));

(b[(2)] = (G__37532 / l));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PNormalize$normalize$arity$2 = (function (_,len){
var self__ = this;
var ___$1 = this;
var G__37533 = self__.buf;
var G__37534 = (G__37533[(0)]);
var G__37535 = (G__37533[(1)]);
var G__37536 = (G__37533[(2)]);
var l = Math.sqrt((((G__37534 * G__37534) + (G__37535 * G__37535)) + (G__37536 * G__37536)));
if((l > (0))){
var l__$1 = (len / l);
var b = (new Float32Array((3)));
(b[(0)] = (G__37534 * l__$1));

(b[(1)] = (G__37535 * l__$1));

(b[(2)] = (G__37536 * l__$1));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PNormalize$normalized_QMARK_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,1.0,thi.ng.geom.core.mag_squared.call(null,___$1));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate3D$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate3D$rotate_x$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
var b = (new Float32Array((3)));
var G__37537 = self__.buf;
var G__37538 = (G__37537[(0)]);
var G__37539 = (G__37537[(1)]);
var G__37540 = (G__37537[(2)]);
(b[(0)] = G__37538);

(b[(1)] = ((G__37539 * c) - (G__37540 * s)));

(b[(2)] = ((G__37539 * s) + (G__37540 * c)));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate3D$rotate_y$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
var b = (new Float32Array((3)));
var G__37541 = self__.buf;
var G__37542 = (G__37541[(0)]);
var G__37543 = (G__37541[(1)]);
var G__37544 = (G__37541[(2)]);
(b[(0)] = ((G__37542 * c) + (G__37544 * s)));

(b[(1)] = G__37543);

(b[(2)] = ((G__37544 * c) - (G__37542 * s)));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate3D$rotate_z$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
var b = (new Float32Array((3)));
var G__37545 = self__.buf;
var G__37546 = (G__37545[(0)]);
var G__37547 = (G__37545[(1)]);
var G__37548 = (G__37545[(2)]);
(b[(0)] = ((G__37546 * c) - (G__37547 * s)));

(b[(1)] = ((G__37546 * s) + (G__37547 * c)));

(b[(2)] = G__37548);

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PRotate3D$rotate_around_axis$arity$3 = (function (_,v,theta){
var self__ = this;
var ___$1 = this;
var G__37549 = self__.buf;
var G__37551 = (G__37549[(0)]);
var G__37552 = (G__37549[(1)]);
var G__37553 = (G__37549[(2)]);
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37550 = v.buf;
var G__37554 = (G__37550[(0)]);
var G__37555 = (G__37550[(1)]);
var G__37556 = (G__37550[(2)]);
var ux_SINGLEQUOTE_ = (G__37554 * G__37551);
var uy_SINGLEQUOTE_ = (G__37554 * G__37552);
var uz_SINGLEQUOTE_ = (G__37554 * G__37553);
var vx_SINGLEQUOTE_ = (G__37555 * G__37551);
var vy_SINGLEQUOTE_ = (G__37555 * G__37552);
var vz_SINGLEQUOTE_ = (G__37555 * G__37553);
var wx_SINGLEQUOTE_ = (G__37556 * G__37551);
var wy_SINGLEQUOTE_ = (G__37556 * G__37552);
var wz_SINGLEQUOTE_ = (G__37556 * G__37553);
var vx2 = (G__37554 * G__37554);
var vy2 = (G__37555 * G__37555);
var vz2 = (G__37556 * G__37556);
var s = Math.sin(theta);
var c = Math.cos(theta);
var uvw = ((ux_SINGLEQUOTE_ + vy_SINGLEQUOTE_) + wz_SINGLEQUOTE_);
var b = (new Float32Array((3)));
(b[(0)] = (((uvw * G__37554) + ((((vy2 + vz2) * G__37551) - ((vy_SINGLEQUOTE_ + wz_SINGLEQUOTE_) * G__37554)) * c)) + ((vz_SINGLEQUOTE_ - wy_SINGLEQUOTE_) * s)));

(b[(1)] = (((uvw * G__37555) + ((((vx2 + vz2) * G__37552) - ((ux_SINGLEQUOTE_ + wz_SINGLEQUOTE_) * G__37555)) * c)) + ((wx_SINGLEQUOTE_ - uz_SINGLEQUOTE_) * s)));

(b[(2)] = (((uvw * G__37556) + ((((vx2 + vy2) * G__37553) - ((ux_SINGLEQUOTE_ + vy_SINGLEQUOTE_) * G__37556)) * c)) + ((uy_SINGLEQUOTE_ - vx_SINGLEQUOTE_) * s)));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
} else {
var G__37554 = cljs.core.nth.call(null,v,(0),0.0);
var G__37555 = cljs.core.nth.call(null,v,(1),0.0);
var G__37556 = cljs.core.nth.call(null,v,(2),0.0);
var ux_SINGLEQUOTE_ = (G__37554 * G__37551);
var uy_SINGLEQUOTE_ = (G__37554 * G__37552);
var uz_SINGLEQUOTE_ = (G__37554 * G__37553);
var vx_SINGLEQUOTE_ = (G__37555 * G__37551);
var vy_SINGLEQUOTE_ = (G__37555 * G__37552);
var vz_SINGLEQUOTE_ = (G__37555 * G__37553);
var wx_SINGLEQUOTE_ = (G__37556 * G__37551);
var wy_SINGLEQUOTE_ = (G__37556 * G__37552);
var wz_SINGLEQUOTE_ = (G__37556 * G__37553);
var vx2 = (G__37554 * G__37554);
var vy2 = (G__37555 * G__37555);
var vz2 = (G__37556 * G__37556);
var s = Math.sin(theta);
var c = Math.cos(theta);
var uvw = ((ux_SINGLEQUOTE_ + vy_SINGLEQUOTE_) + wz_SINGLEQUOTE_);
var b = (new Float32Array((3)));
(b[(0)] = (((uvw * G__37554) + ((((vy2 + vz2) * G__37551) - ((vy_SINGLEQUOTE_ + wz_SINGLEQUOTE_) * G__37554)) * c)) + ((vz_SINGLEQUOTE_ - wy_SINGLEQUOTE_) * s)));

(b[(1)] = (((uvw * G__37555) + ((((vx2 + vz2) * G__37552) - ((ux_SINGLEQUOTE_ + wz_SINGLEQUOTE_) * G__37555)) * c)) + ((wx_SINGLEQUOTE_ - uz_SINGLEQUOTE_) * s)));

(b[(2)] = (((uvw * G__37556) + ((((vx2 + vy2) * G__37553) - ((ux_SINGLEQUOTE_ + vy_SINGLEQUOTE_) * G__37556)) * c)) + ((uy_SINGLEQUOTE_ - vx_SINGLEQUOTE_) * s)));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PClear$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PClear$clear_STAR_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec3((new Float32Array((3))),null,null));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PClear$clear_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = 0.0);

(self__.buf[(1)] = 0.0);

(self__.buf[(2)] = 0.0);

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PReflect$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PReflect$reflect$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((3)));
var G__37557 = self__.buf;
var G__37559 = (G__37557[(0)]);
var G__37560 = (G__37557[(1)]);
var G__37561 = (G__37557[(2)]);
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37558 = v.buf;
var G__37562 = (G__37558[(0)]);
var G__37563 = (G__37558[(1)]);
var G__37564 = (G__37558[(2)]);
var d = ((((G__37559 * G__37562) + (G__37560 * G__37563)) + (G__37561 * G__37564)) * 2.0);
(b[(0)] = ((G__37562 * d) - G__37559));

(b[(1)] = ((G__37563 * d) - G__37560));

(b[(2)] = ((G__37564 * d) - G__37561));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
} else {
var G__37562 = cljs.core.nth.call(null,v,(0),0.0);
var G__37563 = cljs.core.nth.call(null,v,(1),0.0);
var G__37564 = cljs.core.nth.call(null,v,(2),0.0);
var d = ((((G__37559 * G__37562) + (G__37560 * G__37563)) + (G__37561 * G__37564)) * 2.0);
(b[(0)] = ((G__37562 * d) - G__37559));

(b[(1)] = ((G__37563 * d) - G__37560));

(b[(2)] = ((G__37564 * d) - G__37561));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PCrossProduct$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PCrossProduct$cross$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((3)));
var G__37565_37978 = self__.buf;
var G__37567_37979 = (G__37565_37978[(0)]);
var G__37568_37980 = (G__37565_37978[(1)]);
var G__37569_37981 = (G__37565_37978[(2)]);
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37566_37982 = v.buf;
var G__37570_37983 = (G__37566_37982[(0)]);
var G__37571_37984 = (G__37566_37982[(1)]);
var G__37572_37985 = (G__37566_37982[(2)]);
(b[(0)] = ((G__37568_37980 * G__37572_37985) - (G__37571_37984 * G__37569_37981)));

(b[(1)] = ((G__37569_37981 * G__37570_37983) - (G__37572_37985 * G__37567_37979)));

(b[(2)] = ((G__37567_37979 * G__37571_37984) - (G__37570_37983 * G__37568_37980)));
} else {
var G__37570_37986 = cljs.core.nth.call(null,v,(0),0.0);
var G__37571_37987 = cljs.core.nth.call(null,v,(1),0.0);
var G__37572_37988 = cljs.core.nth.call(null,v,(2),0.0);
(b[(0)] = ((G__37568_37980 * G__37572_37988) - (G__37571_37987 * G__37569_37981)));

(b[(1)] = ((G__37569_37981 * G__37570_37986) - (G__37572_37988 * G__37567_37979)));

(b[(2)] = ((G__37567_37979 * G__37571_37987) - (G__37570_37986 * G__37568_37980)));
}

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IIndexed$_nth$arity$2 = (function (_,n){
var self__ = this;
var ___$1 = this;
if((n >= (0))){
if((n < (3))){
return (self__.buf[n]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,n);
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IIndexed$_nth$arity$3 = (function (_,n,nf){
var self__ = this;
var ___$1 = this;
if((n >= (0))){
if((n < (3))){
return (self__.buf[n]);
} else {
return nf;
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IVector$_assoc_n$arity$3 = (function (_,n,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array(self__.buf));
(b[n] = v);

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__._meta;
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ICloneable$_clone$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec3((new Float32Array(self__.buf)),self__._hash,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$INext$_next$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.cons.call(null,(self__.buf[(1)]),cljs.core.cons.call(null,(self__.buf[(2)]),null));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ICounted$_count$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (3);
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IStack$_peek$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (self__.buf[(2)]);
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IStack$_pop$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((2)));
(b[(0)] = (self__.buf[(0)]));

(b[(1)] = (self__.buf[(1)]));

return (new thi.ng.geom.core.vector.Vec2(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = (- (self__.buf[(0)])));

(self__.buf[(1)] = (- (self__.buf[(1)])));

(self__.buf[(2)] = (- (self__.buf[(2)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37573_37989 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37574_37990 = v.buf;
(self__.buf[(0)] = ((G__37573_37989[(0)]) - (G__37574_37990[(0)])));

(self__.buf[(1)] = ((G__37573_37989[(1)]) - (G__37574_37990[(1)])));

(self__.buf[(2)] = ((G__37573_37989[(2)]) - (G__37574_37990[(2)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__37573_37989[(0)]) - v));

(self__.buf[(1)] = ((G__37573_37989[(1)]) - v));

(self__.buf[(2)] = ((G__37573_37989[(2)]) - v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__37573_37989[(0)]) - cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__37573_37989[(1)]) - cljs.core.nth.call(null,v,(1),0.0)));

(self__.buf[(2)] = ((G__37573_37989[(2)]) - cljs.core.nth.call(null,v,(2),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37587_37991 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37588_37992 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37589_37993 = ((!(G__37587_37991))?typeof v1 === 'number':null);
var G__37590_37994 = ((!(G__37588_37992))?typeof v2 === 'number':null);
var G__37575_37995 = self__.buf;
var G__37576_37996 = ((G__37587_37991)?v1.buf:null);
var G__37577_37997 = ((G__37588_37992)?v2.buf:null);
var G__37578_37998 = (G__37575_37995[(0)]);
var G__37579_37999 = (G__37575_37995[(1)]);
var G__37580_38000 = (G__37575_37995[(2)]);
var G__37581_38001 = ((G__37587_37991)?(G__37576_37996[(0)]):(cljs.core.truth_(G__37589_37993)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37582_38002 = ((G__37587_37991)?(G__37576_37996[(1)]):(cljs.core.truth_(G__37589_37993)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37583_38003 = ((G__37587_37991)?(G__37576_37996[(2)]):(cljs.core.truth_(G__37589_37993)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37584_38004 = ((G__37588_37992)?(G__37577_37997[(0)]):(cljs.core.truth_(G__37590_37994)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37585_38005 = ((G__37588_37992)?(G__37577_37997[(1)]):(cljs.core.truth_(G__37590_37994)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37586_38006 = ((G__37588_37992)?(G__37577_37997[(2)]):(cljs.core.truth_(G__37590_37994)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(self__.buf[(0)] = ((G__37578_37998 - G__37581_38001) - G__37584_38004));

(self__.buf[(1)] = ((G__37579_37999 - G__37582_38002) - G__37585_38005));

(self__.buf[(2)] = ((G__37580_38000 - G__37583_38003) - G__37586_38006));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$__BANG_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((self__.buf[(0)]) - x));

(self__.buf[(1)] = ((self__.buf[(1)]) - y));

(self__.buf[(2)] = ((self__.buf[(2)]) - z));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37591_38007 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37592_38008 = v.buf;
(self__.buf[(0)] = ((G__37591_38007[(0)]) * (G__37592_38008[(0)])));

(self__.buf[(1)] = ((G__37591_38007[(1)]) * (G__37592_38008[(1)])));

(self__.buf[(2)] = ((G__37591_38007[(2)]) * (G__37592_38008[(2)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__37591_38007[(0)]) * v));

(self__.buf[(1)] = ((G__37591_38007[(1)]) * v));

(self__.buf[(2)] = ((G__37591_38007[(2)]) * v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__37591_38007[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__37591_38007[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));

(self__.buf[(2)] = ((G__37591_38007[(2)]) * cljs.core.nth.call(null,v,(2),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37605_38009 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37606_38010 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37607_38011 = ((!(G__37605_38009))?typeof v1 === 'number':null);
var G__37608_38012 = ((!(G__37606_38010))?typeof v2 === 'number':null);
var G__37593_38013 = self__.buf;
var G__37594_38014 = ((G__37605_38009)?v1.buf:null);
var G__37595_38015 = ((G__37606_38010)?v2.buf:null);
var G__37596_38016 = (G__37593_38013[(0)]);
var G__37597_38017 = (G__37593_38013[(1)]);
var G__37598_38018 = (G__37593_38013[(2)]);
var G__37599_38019 = ((G__37605_38009)?(G__37594_38014[(0)]):(cljs.core.truth_(G__37607_38011)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37600_38020 = ((G__37605_38009)?(G__37594_38014[(1)]):(cljs.core.truth_(G__37607_38011)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37601_38021 = ((G__37605_38009)?(G__37594_38014[(2)]):(cljs.core.truth_(G__37607_38011)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37602_38022 = ((G__37606_38010)?(G__37595_38015[(0)]):(cljs.core.truth_(G__37608_38012)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37603_38023 = ((G__37606_38010)?(G__37595_38015[(1)]):(cljs.core.truth_(G__37608_38012)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37604_38024 = ((G__37606_38010)?(G__37595_38015[(2)]):(cljs.core.truth_(G__37608_38012)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(self__.buf[(0)] = ((G__37596_38016 * G__37599_38019) * G__37602_38022));

(self__.buf[(1)] = ((G__37597_38017 * G__37600_38020) * G__37603_38023));

(self__.buf[(2)] = ((G__37598_38018 * G__37601_38021) * G__37604_38024));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_STAR__BANG_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((self__.buf[(0)]) * x));

(self__.buf[(1)] = ((self__.buf[(1)]) * y));

(self__.buf[(2)] = ((self__.buf[(2)]) * z));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$subm_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__37621_38025 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37622_38026 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37623_38027 = ((!(G__37621_38025))?typeof a === 'number':null);
var G__37624_38028 = ((!(G__37622_38026))?typeof b === 'number':null);
var G__37609_38029 = self__.buf;
var G__37610_38030 = ((G__37621_38025)?a.buf:null);
var G__37611_38031 = ((G__37622_38026)?b.buf:null);
var G__37612_38032 = (G__37609_38029[(0)]);
var G__37613_38033 = (G__37609_38029[(1)]);
var G__37614_38034 = (G__37609_38029[(2)]);
var G__37615_38035 = ((G__37621_38025)?(G__37610_38030[(0)]):(cljs.core.truth_(G__37623_38027)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37616_38036 = ((G__37621_38025)?(G__37610_38030[(1)]):(cljs.core.truth_(G__37623_38027)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37617_38037 = ((G__37621_38025)?(G__37610_38030[(2)]):(cljs.core.truth_(G__37623_38027)?a:cljs.core.nth.call(null,a,(2),0.0)));
var G__37618_38038 = ((G__37622_38026)?(G__37611_38031[(0)]):(cljs.core.truth_(G__37624_38028)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37619_38039 = ((G__37622_38026)?(G__37611_38031[(1)]):(cljs.core.truth_(G__37624_38028)?b:cljs.core.nth.call(null,b,(1),1.0)));
var G__37620_38040 = ((G__37622_38026)?(G__37611_38031[(2)]):(cljs.core.truth_(G__37624_38028)?b:cljs.core.nth.call(null,b,(2),1.0)));
(self__.buf[(0)] = ((G__37612_38032 - G__37615_38035) * G__37618_38038));

(self__.buf[(1)] = ((G__37613_38033 - G__37616_38036) * G__37619_38039));

(self__.buf[(2)] = ((G__37614_38034 - G__37617_38037) * G__37620_38040));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$msub_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__37637_38041 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37638_38042 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37639_38043 = ((!(G__37637_38041))?typeof a === 'number':null);
var G__37640_38044 = ((!(G__37638_38042))?typeof b === 'number':null);
var G__37625_38045 = self__.buf;
var G__37626_38046 = ((G__37637_38041)?a.buf:null);
var G__37627_38047 = ((G__37638_38042)?b.buf:null);
var G__37628_38048 = (G__37625_38045[(0)]);
var G__37629_38049 = (G__37625_38045[(1)]);
var G__37630_38050 = (G__37625_38045[(2)]);
var G__37631_38051 = ((G__37637_38041)?(G__37626_38046[(0)]):(cljs.core.truth_(G__37639_38043)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37632_38052 = ((G__37637_38041)?(G__37626_38046[(1)]):(cljs.core.truth_(G__37639_38043)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37633_38053 = ((G__37637_38041)?(G__37626_38046[(2)]):(cljs.core.truth_(G__37639_38043)?a:cljs.core.nth.call(null,a,(2),1.0)));
var G__37634_38054 = ((G__37638_38042)?(G__37627_38047[(0)]):(cljs.core.truth_(G__37640_38044)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37635_38055 = ((G__37638_38042)?(G__37627_38047[(1)]):(cljs.core.truth_(G__37640_38044)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37636_38056 = ((G__37638_38042)?(G__37627_38047[(2)]):(cljs.core.truth_(G__37640_38044)?b:cljs.core.nth.call(null,b,(2),0.0)));
(self__.buf[(0)] = ((G__37628_38048 * G__37631_38051) - G__37634_38054));

(self__.buf[(1)] = ((G__37629_38049 * G__37632_38052) - G__37635_38055));

(self__.buf[(2)] = ((G__37630_38050 * G__37633_38053) - G__37636_38056));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$abs_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = thi.ng.math.core.abs.call(null,(self__.buf[(0)])));

(self__.buf[(1)] = thi.ng.math.core.abs.call(null,(self__.buf[(1)])));

(self__.buf[(2)] = thi.ng.math.core.abs.call(null,(self__.buf[(2)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$madd_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__37653_38057 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37654_38058 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37655_38059 = ((!(G__37653_38057))?typeof a === 'number':null);
var G__37656_38060 = ((!(G__37654_38058))?typeof b === 'number':null);
var G__37641_38061 = self__.buf;
var G__37642_38062 = ((G__37653_38057)?a.buf:null);
var G__37643_38063 = ((G__37654_38058)?b.buf:null);
var G__37644_38064 = (G__37641_38061[(0)]);
var G__37645_38065 = (G__37641_38061[(1)]);
var G__37646_38066 = (G__37641_38061[(2)]);
var G__37647_38067 = ((G__37653_38057)?(G__37642_38062[(0)]):(cljs.core.truth_(G__37655_38059)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37648_38068 = ((G__37653_38057)?(G__37642_38062[(1)]):(cljs.core.truth_(G__37655_38059)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37649_38069 = ((G__37653_38057)?(G__37642_38062[(2)]):(cljs.core.truth_(G__37655_38059)?a:cljs.core.nth.call(null,a,(2),1.0)));
var G__37650_38070 = ((G__37654_38058)?(G__37643_38063[(0)]):(cljs.core.truth_(G__37656_38060)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37651_38071 = ((G__37654_38058)?(G__37643_38063[(1)]):(cljs.core.truth_(G__37656_38060)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37652_38072 = ((G__37654_38058)?(G__37643_38063[(2)]):(cljs.core.truth_(G__37656_38060)?b:cljs.core.nth.call(null,b,(2),0.0)));
(self__.buf[(0)] = ((G__37644_38064 * G__37647_38067) + G__37650_38070));

(self__.buf[(1)] = ((G__37645_38065 * G__37648_38068) + G__37651_38071));

(self__.buf[(2)] = ((G__37646_38066 * G__37649_38069) + G__37652_38072));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((1) / (self__.buf[(0)])));

(self__.buf[(1)] = ((1) / (self__.buf[(1)])));

(self__.buf[(2)] = ((1) / (self__.buf[(2)])));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37657_38073 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37658_38074 = v.buf;
(self__.buf[(0)] = ((G__37657_38073[(0)]) / (G__37658_38074[(0)])));

(self__.buf[(1)] = ((G__37657_38073[(1)]) / (G__37658_38074[(1)])));

(self__.buf[(2)] = ((G__37657_38073[(2)]) / (G__37658_38074[(2)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__37657_38073[(0)]) / v));

(self__.buf[(1)] = ((G__37657_38073[(1)]) / v));

(self__.buf[(2)] = ((G__37657_38073[(2)]) / v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__37657_38073[(0)]) / cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__37657_38073[(1)]) / cljs.core.nth.call(null,v,(1),0.0)));

(self__.buf[(2)] = ((G__37657_38073[(2)]) / cljs.core.nth.call(null,v,(2),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37671_38075 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37672_38076 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37673_38077 = ((!(G__37671_38075))?typeof v1 === 'number':null);
var G__37674_38078 = ((!(G__37672_38076))?typeof v2 === 'number':null);
var G__37659_38079 = self__.buf;
var G__37660_38080 = ((G__37671_38075)?v1.buf:null);
var G__37661_38081 = ((G__37672_38076)?v2.buf:null);
var G__37662_38082 = (G__37659_38079[(0)]);
var G__37663_38083 = (G__37659_38079[(1)]);
var G__37664_38084 = (G__37659_38079[(2)]);
var G__37665_38085 = ((G__37671_38075)?(G__37660_38080[(0)]):(cljs.core.truth_(G__37673_38077)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37666_38086 = ((G__37671_38075)?(G__37660_38080[(1)]):(cljs.core.truth_(G__37673_38077)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37667_38087 = ((G__37671_38075)?(G__37660_38080[(2)]):(cljs.core.truth_(G__37673_38077)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37668_38088 = ((G__37672_38076)?(G__37661_38081[(0)]):(cljs.core.truth_(G__37674_38078)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37669_38089 = ((G__37672_38076)?(G__37661_38081[(1)]):(cljs.core.truth_(G__37674_38078)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37670_38090 = ((G__37672_38076)?(G__37661_38081[(2)]):(cljs.core.truth_(G__37674_38078)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(self__.buf[(0)] = ((G__37662_38082 / G__37665_38085) / G__37668_38088));

(self__.buf[(1)] = ((G__37663_38083 / G__37666_38086) / G__37669_38089));

(self__.buf[(2)] = ((G__37664_38084 / G__37667_38087) / G__37670_38090));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$div_BANG_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((self__.buf[(0)]) / x));

(self__.buf[(1)] = ((self__.buf[(1)]) / y));

(self__.buf[(2)] = ((self__.buf[(2)]) / z));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37675_38091 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37676_38092 = v.buf;
(self__.buf[(0)] = ((G__37675_38091[(0)]) + (G__37676_38092[(0)])));

(self__.buf[(1)] = ((G__37675_38091[(1)]) + (G__37676_38092[(1)])));

(self__.buf[(2)] = ((G__37675_38091[(2)]) + (G__37676_38092[(2)])));

self__._hash = null;
} else {
if(typeof v === 'number'){
(self__.buf[(0)] = ((G__37675_38091[(0)]) + v));

(self__.buf[(1)] = ((G__37675_38091[(1)]) + v));

(self__.buf[(2)] = ((G__37675_38091[(2)]) + v));

self__._hash = null;
} else {
(self__.buf[(0)] = ((G__37675_38091[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(self__.buf[(1)] = ((G__37675_38091[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));

(self__.buf[(2)] = ((G__37675_38091[(2)]) + cljs.core.nth.call(null,v,(2),0.0)));

self__._hash = null;
}
}

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var G__37689_38093 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37690_38094 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37691_38095 = ((!(G__37689_38093))?typeof v1 === 'number':null);
var G__37692_38096 = ((!(G__37690_38094))?typeof v2 === 'number':null);
var G__37677_38097 = self__.buf;
var G__37678_38098 = ((G__37689_38093)?v1.buf:null);
var G__37679_38099 = ((G__37690_38094)?v2.buf:null);
var G__37680_38100 = (G__37677_38097[(0)]);
var G__37681_38101 = (G__37677_38097[(1)]);
var G__37682_38102 = (G__37677_38097[(2)]);
var G__37683_38103 = ((G__37689_38093)?(G__37678_38098[(0)]):(cljs.core.truth_(G__37691_38095)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37684_38104 = ((G__37689_38093)?(G__37678_38098[(1)]):(cljs.core.truth_(G__37691_38095)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37685_38105 = ((G__37689_38093)?(G__37678_38098[(2)]):(cljs.core.truth_(G__37691_38095)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37686_38106 = ((G__37690_38094)?(G__37679_38099[(0)]):(cljs.core.truth_(G__37692_38096)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37687_38107 = ((G__37690_38094)?(G__37679_38099[(1)]):(cljs.core.truth_(G__37692_38096)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37688_38108 = ((G__37690_38094)?(G__37679_38099[(2)]):(cljs.core.truth_(G__37692_38096)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(self__.buf[(0)] = ((G__37680_38100 + G__37683_38103) + G__37686_38106));

(self__.buf[(1)] = ((G__37681_38101 + G__37684_38104) + G__37687_38107));

(self__.buf[(2)] = ((G__37682_38102 + G__37685_38105) + G__37688_38108));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$_PLUS__BANG_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
(self__.buf[(0)] = ((self__.buf[(0)]) + x));

(self__.buf[(1)] = ((self__.buf[(1)]) + y));

(self__.buf[(2)] = ((self__.buf[(2)]) + z));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMutableMathOps$addm_BANG_$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var G__37705_38109 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37706_38110 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37707_38111 = ((!(G__37705_38109))?typeof a === 'number':null);
var G__37708_38112 = ((!(G__37706_38110))?typeof b === 'number':null);
var G__37693_38113 = self__.buf;
var G__37694_38114 = ((G__37705_38109)?a.buf:null);
var G__37695_38115 = ((G__37706_38110)?b.buf:null);
var G__37696_38116 = (G__37693_38113[(0)]);
var G__37697_38117 = (G__37693_38113[(1)]);
var G__37698_38118 = (G__37693_38113[(2)]);
var G__37699_38119 = ((G__37705_38109)?(G__37694_38114[(0)]):(cljs.core.truth_(G__37707_38111)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37700_38120 = ((G__37705_38109)?(G__37694_38114[(1)]):(cljs.core.truth_(G__37707_38111)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37701_38121 = ((G__37705_38109)?(G__37694_38114[(2)]):(cljs.core.truth_(G__37707_38111)?a:cljs.core.nth.call(null,a,(2),0.0)));
var G__37702_38122 = ((G__37706_38110)?(G__37695_38115[(0)]):(cljs.core.truth_(G__37708_38112)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37703_38123 = ((G__37706_38110)?(G__37695_38115[(1)]):(cljs.core.truth_(G__37708_38112)?b:cljs.core.nth.call(null,b,(1),1.0)));
var G__37704_38124 = ((G__37706_38110)?(G__37695_38115[(2)]):(cljs.core.truth_(G__37708_38112)?b:cljs.core.nth.call(null,b,(2),1.0)));
(self__.buf[(0)] = ((G__37696_38116 + G__37699_38119) * G__37702_38122));

(self__.buf[(1)] = ((G__37697_38117 + G__37700_38120) * G__37703_38123));

(self__.buf[(2)] = ((G__37698_38118 + G__37701_38121) * G__37704_38124));

self__._hash = null;

return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IReversible$_rseq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.vector.swizzle3_fns.call(null,new cljs.core.Keyword(null,"zyx","zyx",1752527951)).call(null,___$1);
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IHash$_hash$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var or__16069__auto__ = self__._hash;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return ___$1._hash = cljs.core.mix_collection_hash.call(null,((cljs.core.imul.call(null,((cljs.core.imul.call(null,(((31) + cljs.core.hash.call(null,(self__.buf[(0)]))) | (0)),(31)) + cljs.core.hash.call(null,(self__.buf[(1)]))) | (0)),(31)) + cljs.core.hash.call(null,(self__.buf[(2)]))) | (0)),(3));
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IEquiv$_equiv$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
if((o instanceof thi.ng.geom.core.vector.Vec3)){
var b_SINGLEQUOTE_ = o.buf;
return (((self__.buf[(0)]) === (b_SINGLEQUOTE_[(0)]))) && (((self__.buf[(1)]) === (b_SINGLEQUOTE_[(1)]))) && (((self__.buf[(2)]) === (b_SINGLEQUOTE_[(2)])));
} else {
return (cljs.core.sequential_QMARK_.call(null,o)) && (((3) === cljs.core.count.call(null,o))) && (cljs.core._EQ_.call(null,(self__.buf[(0)]),cljs.core.first.call(null,o))) && (cljs.core._EQ_.call(null,(self__.buf[(1)]),cljs.core.nth.call(null,o,(1)))) && (cljs.core._EQ_.call(null,(self__.buf[(2)]),cljs.core.nth.call(null,o,(2))));
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$heading$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.heading_xy.call(null,___$1);
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$heading_xy$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var t = Math.atan2((self__.buf[(1)]),(self__.buf[(0)]));
if((t < (0))){
return (t + thi.ng.math.core.TWO_PI);
} else {
return t;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$heading_xz$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var t = Math.atan2((self__.buf[(2)]),(self__.buf[(0)]));
if((t < (0))){
return (t + thi.ng.math.core.TWO_PI);
} else {
return t;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$heading_yz$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var t = Math.atan2((self__.buf[(2)]),(self__.buf[(1)]));
if((t < (0))){
return (t + thi.ng.math.core.TWO_PI);
} else {
return t;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$angle_between$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var v__$1 = (((v instanceof thi.ng.geom.core.vector.Vec3))?v:thi.ng.geom.core.vector.vec3.call(null,v));
return Math.acos(thi.ng.geom.core.dot.call(null,thi.ng.geom.core.normalize.call(null,___$1),thi.ng.geom.core.normalize.call(null,v__$1)));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$slope_xy$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ((self__.buf[(1)]) / (self__.buf[(0)]));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$slope_xz$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ((self__.buf[(2)]) / (self__.buf[(0)]));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PHeading$slope_yz$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ((self__.buf[(2)]) / (self__.buf[(1)]));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PDistance$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PDistance$dist$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
return Math.sqrt(thi.ng.geom.core.dist_squared.call(null,___$1,v));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PDistance$dist_squared$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var G__37709 = self__.buf;
var G__37711 = (G__37709[(0)]);
var G__37712 = (G__37709[(1)]);
var G__37713 = (G__37709[(2)]);
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37710 = v.buf;
var G__37714 = (G__37710[(0)]);
var G__37715 = (G__37710[(1)]);
var G__37716 = (G__37710[(2)]);
var dx = (G__37711 - G__37714);
var dy = (G__37712 - G__37715);
var dz = (G__37713 - G__37716);
return (((dx * dx) + (dy * dy)) + (dz * dz));
} else {
var G__37714 = cljs.core.nth.call(null,v,(0),0.0);
var G__37715 = cljs.core.nth.call(null,v,(1),0.0);
var G__37716 = cljs.core.nth.call(null,v,(2),0.0);
var dx = (G__37711 - G__37714);
var dy = (G__37712 - G__37715);
var dz = (G__37713 - G__37716);
return (((dx * dx) + (dy * dy)) + (dz * dz));
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IReduce$_reduce$arity$2 = (function (coll,f){
var self__ = this;
var coll__$1 = this;
var acc = f.call(null,(self__.buf[(0)]),(self__.buf[(1)]));
if(cljs.core.reduced_QMARK_.call(null,acc)){
return cljs.core.deref.call(null,acc);
} else {
var acc__$1 = f.call(null,acc,(self__.buf[(2)]));
if(cljs.core.reduced_QMARK_.call(null,acc__$1)){
return cljs.core.deref.call(null,acc__$1);
} else {
return acc__$1;
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IReduce$_reduce$arity$3 = (function (coll,f,start){
var self__ = this;
var coll__$1 = this;
var acc = f.call(null,start,(self__.buf[(0)]));
if(cljs.core.reduced_QMARK_.call(null,acc)){
return cljs.core.deref.call(null,acc);
} else {
var acc__$1 = f.call(null,acc,(self__.buf[(1)]));
if(cljs.core.reduced_QMARK_.call(null,acc__$1)){
return cljs.core.deref.call(null,acc__$1);
} else {
var acc__$2 = f.call(null,acc__$1,(self__.buf[(2)]));
if(cljs.core.reduced_QMARK_.call(null,acc__$2)){
return cljs.core.deref.call(null,acc__$2);
} else {
return acc__$2;
}
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInvert$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInvert$invert$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._.call(null,___$1);
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$msub$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37729_38125 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37730_38126 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37731_38127 = ((!(G__37729_38125))?typeof a === 'number':null);
var G__37732_38128 = ((!(G__37730_38126))?typeof b === 'number':null);
var G__37717_38129 = self__.buf;
var G__37718_38130 = ((G__37729_38125)?a.buf:null);
var G__37719_38131 = ((G__37730_38126)?b.buf:null);
var G__37720_38132 = (G__37717_38129[(0)]);
var G__37721_38133 = (G__37717_38129[(1)]);
var G__37722_38134 = (G__37717_38129[(2)]);
var G__37723_38135 = ((G__37729_38125)?(G__37718_38130[(0)]):(cljs.core.truth_(G__37731_38127)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37724_38136 = ((G__37729_38125)?(G__37718_38130[(1)]):(cljs.core.truth_(G__37731_38127)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37725_38137 = ((G__37729_38125)?(G__37718_38130[(2)]):(cljs.core.truth_(G__37731_38127)?a:cljs.core.nth.call(null,a,(2),1.0)));
var G__37726_38138 = ((G__37730_38126)?(G__37719_38131[(0)]):(cljs.core.truth_(G__37732_38128)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37727_38139 = ((G__37730_38126)?(G__37719_38131[(1)]):(cljs.core.truth_(G__37732_38128)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37728_38140 = ((G__37730_38126)?(G__37719_38131[(2)]):(cljs.core.truth_(G__37732_38128)?b:cljs.core.nth.call(null,b,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37720_38132 * G__37723_38135) - G__37726_38138));

(dest__18831__auto__[(1)] = ((G__37721_38133 * G__37724_38136) - G__37727_38139));

(dest__18831__auto__[(2)] = ((G__37722_38134 * G__37725_38137) - G__37728_38140));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37733_38141 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37734_38142 = v.buf;
(dest__18820__auto__[(0)] = ((G__37733_38141[(0)]) * (G__37734_38142[(0)])));

(dest__18820__auto__[(1)] = ((G__37733_38141[(1)]) * (G__37734_38142[(1)])));

(dest__18820__auto__[(2)] = ((G__37733_38141[(2)]) * (G__37734_38142[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37733_38141[(0)]) * v));

(dest__18820__auto__[(1)] = ((G__37733_38141[(1)]) * v));

(dest__18820__auto__[(2)] = ((G__37733_38141[(2)]) * v));
} else {
(dest__18820__auto__[(0)] = ((G__37733_38141[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37733_38141[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37733_38141[(2)]) * cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37747_38143 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37748_38144 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37749_38145 = ((!(G__37747_38143))?typeof v1 === 'number':null);
var G__37750_38146 = ((!(G__37748_38144))?typeof v2 === 'number':null);
var G__37735_38147 = self__.buf;
var G__37736_38148 = ((G__37747_38143)?v1.buf:null);
var G__37737_38149 = ((G__37748_38144)?v2.buf:null);
var G__37738_38150 = (G__37735_38147[(0)]);
var G__37739_38151 = (G__37735_38147[(1)]);
var G__37740_38152 = (G__37735_38147[(2)]);
var G__37741_38153 = ((G__37747_38143)?(G__37736_38148[(0)]):(cljs.core.truth_(G__37749_38145)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37742_38154 = ((G__37747_38143)?(G__37736_38148[(1)]):(cljs.core.truth_(G__37749_38145)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37743_38155 = ((G__37747_38143)?(G__37736_38148[(2)]):(cljs.core.truth_(G__37749_38145)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37744_38156 = ((G__37748_38144)?(G__37737_38149[(0)]):(cljs.core.truth_(G__37750_38146)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37745_38157 = ((G__37748_38144)?(G__37737_38149[(1)]):(cljs.core.truth_(G__37750_38146)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37746_38158 = ((G__37748_38144)?(G__37737_38149[(2)]):(cljs.core.truth_(G__37750_38146)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37738_38150 * G__37741_38153) * G__37744_38156));

(dest__18831__auto__[(1)] = ((G__37739_38151 * G__37742_38154) * G__37745_38157));

(dest__18831__auto__[(2)] = ((G__37740_38152 * G__37743_38155) * G__37746_38158));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37751 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37751[(0)]) * x));

(dest__18809__auto__[(1)] = ((G__37751[(1)]) * y));

(dest__18809__auto__[(2)] = ((G__37751[(2)]) * z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18803__auto__ = (new Float32Array((3)));
var G__37752_38159 = self__.buf;
(dest__18803__auto__[(0)] = (- (G__37752_38159[(0)])));

(dest__18803__auto__[(1)] = (- (G__37752_38159[(1)])));

(dest__18803__auto__[(2)] = (- (G__37752_38159[(2)])));

return (new thi.ng.geom.core.vector.Vec3(dest__18803__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37753_38160 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37754_38161 = v.buf;
(dest__18820__auto__[(0)] = ((G__37753_38160[(0)]) - (G__37754_38161[(0)])));

(dest__18820__auto__[(1)] = ((G__37753_38160[(1)]) - (G__37754_38161[(1)])));

(dest__18820__auto__[(2)] = ((G__37753_38160[(2)]) - (G__37754_38161[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37753_38160[(0)]) - v));

(dest__18820__auto__[(1)] = ((G__37753_38160[(1)]) - v));

(dest__18820__auto__[(2)] = ((G__37753_38160[(2)]) - v));
} else {
(dest__18820__auto__[(0)] = ((G__37753_38160[(0)]) - cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37753_38160[(1)]) - cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37753_38160[(2)]) - cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37767_38162 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37768_38163 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37769_38164 = ((!(G__37767_38162))?typeof v1 === 'number':null);
var G__37770_38165 = ((!(G__37768_38163))?typeof v2 === 'number':null);
var G__37755_38166 = self__.buf;
var G__37756_38167 = ((G__37767_38162)?v1.buf:null);
var G__37757_38168 = ((G__37768_38163)?v2.buf:null);
var G__37758_38169 = (G__37755_38166[(0)]);
var G__37759_38170 = (G__37755_38166[(1)]);
var G__37760_38171 = (G__37755_38166[(2)]);
var G__37761_38172 = ((G__37767_38162)?(G__37756_38167[(0)]):(cljs.core.truth_(G__37769_38164)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37762_38173 = ((G__37767_38162)?(G__37756_38167[(1)]):(cljs.core.truth_(G__37769_38164)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37763_38174 = ((G__37767_38162)?(G__37756_38167[(2)]):(cljs.core.truth_(G__37769_38164)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37764_38175 = ((G__37768_38163)?(G__37757_38168[(0)]):(cljs.core.truth_(G__37770_38165)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37765_38176 = ((G__37768_38163)?(G__37757_38168[(1)]):(cljs.core.truth_(G__37770_38165)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37766_38177 = ((G__37768_38163)?(G__37757_38168[(2)]):(cljs.core.truth_(G__37770_38165)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37758_38169 - G__37761_38172) - G__37764_38175));

(dest__18831__auto__[(1)] = ((G__37759_38170 - G__37762_38173) - G__37765_38176));

(dest__18831__auto__[(2)] = ((G__37760_38171 - G__37763_38174) - G__37766_38177));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37771 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37771[(0)]) - x));

(dest__18809__auto__[(1)] = ((G__37771[(1)]) - y));

(dest__18809__auto__[(2)] = ((G__37771[(2)]) - z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$madd$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37784_38178 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37785_38179 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37786_38180 = ((!(G__37784_38178))?typeof a === 'number':null);
var G__37787_38181 = ((!(G__37785_38179))?typeof b === 'number':null);
var G__37772_38182 = self__.buf;
var G__37773_38183 = ((G__37784_38178)?a.buf:null);
var G__37774_38184 = ((G__37785_38179)?b.buf:null);
var G__37775_38185 = (G__37772_38182[(0)]);
var G__37776_38186 = (G__37772_38182[(1)]);
var G__37777_38187 = (G__37772_38182[(2)]);
var G__37778_38188 = ((G__37784_38178)?(G__37773_38183[(0)]):(cljs.core.truth_(G__37786_38180)?a:cljs.core.nth.call(null,a,(0),1.0)));
var G__37779_38189 = ((G__37784_38178)?(G__37773_38183[(1)]):(cljs.core.truth_(G__37786_38180)?a:cljs.core.nth.call(null,a,(1),1.0)));
var G__37780_38190 = ((G__37784_38178)?(G__37773_38183[(2)]):(cljs.core.truth_(G__37786_38180)?a:cljs.core.nth.call(null,a,(2),1.0)));
var G__37781_38191 = ((G__37785_38179)?(G__37774_38184[(0)]):(cljs.core.truth_(G__37787_38181)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37782_38192 = ((G__37785_38179)?(G__37774_38184[(1)]):(cljs.core.truth_(G__37787_38181)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37783_38193 = ((G__37785_38179)?(G__37774_38184[(2)]):(cljs.core.truth_(G__37787_38181)?b:cljs.core.nth.call(null,b,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37775_38185 * G__37778_38188) + G__37781_38191));

(dest__18831__auto__[(1)] = ((G__37776_38186 * G__37779_38189) + G__37782_38192));

(dest__18831__auto__[(2)] = ((G__37777_38187 * G__37780_38190) + G__37783_38193));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$addm$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37800_38194 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37801_38195 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37802_38196 = ((!(G__37800_38194))?typeof a === 'number':null);
var G__37803_38197 = ((!(G__37801_38195))?typeof b === 'number':null);
var G__37788_38198 = self__.buf;
var G__37789_38199 = ((G__37800_38194)?a.buf:null);
var G__37790_38200 = ((G__37801_38195)?b.buf:null);
var G__37791_38201 = (G__37788_38198[(0)]);
var G__37792_38202 = (G__37788_38198[(1)]);
var G__37793_38203 = (G__37788_38198[(2)]);
var G__37794_38204 = ((G__37800_38194)?(G__37789_38199[(0)]):(cljs.core.truth_(G__37802_38196)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37795_38205 = ((G__37800_38194)?(G__37789_38199[(1)]):(cljs.core.truth_(G__37802_38196)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37796_38206 = ((G__37800_38194)?(G__37789_38199[(2)]):(cljs.core.truth_(G__37802_38196)?a:cljs.core.nth.call(null,a,(2),0.0)));
var G__37797_38207 = ((G__37801_38195)?(G__37790_38200[(0)]):(cljs.core.truth_(G__37803_38197)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37798_38208 = ((G__37801_38195)?(G__37790_38200[(1)]):(cljs.core.truth_(G__37803_38197)?b:cljs.core.nth.call(null,b,(1),1.0)));
var G__37799_38209 = ((G__37801_38195)?(G__37790_38200[(2)]):(cljs.core.truth_(G__37803_38197)?b:cljs.core.nth.call(null,b,(2),1.0)));
(dest__18831__auto__[(0)] = ((G__37791_38201 + G__37794_38204) * G__37797_38207));

(dest__18831__auto__[(1)] = ((G__37792_38202 + G__37795_38205) * G__37798_38208));

(dest__18831__auto__[(2)] = ((G__37793_38203 + G__37796_38206) * G__37799_38209));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$div$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18803__auto__ = (new Float32Array((3)));
var G__37804_38210 = self__.buf;
(dest__18803__auto__[(0)] = ((1) / (G__37804_38210[(0)])));

(dest__18803__auto__[(1)] = ((1) / (G__37804_38210[(1)])));

(dest__18803__auto__[(2)] = ((1) / (G__37804_38210[(2)])));

return (new thi.ng.geom.core.vector.Vec3(dest__18803__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$div$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37805_38211 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37806_38212 = v.buf;
(dest__18820__auto__[(0)] = ((G__37805_38211[(0)]) / (G__37806_38212[(0)])));

(dest__18820__auto__[(1)] = ((G__37805_38211[(1)]) / (G__37806_38212[(1)])));

(dest__18820__auto__[(2)] = ((G__37805_38211[(2)]) / (G__37806_38212[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37805_38211[(0)]) / v));

(dest__18820__auto__[(1)] = ((G__37805_38211[(1)]) / v));

(dest__18820__auto__[(2)] = ((G__37805_38211[(2)]) / v));
} else {
(dest__18820__auto__[(0)] = ((G__37805_38211[(0)]) / cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37805_38211[(1)]) / cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37805_38211[(2)]) / cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$div$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37819_38213 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37820_38214 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37821_38215 = ((!(G__37819_38213))?typeof v1 === 'number':null);
var G__37822_38216 = ((!(G__37820_38214))?typeof v2 === 'number':null);
var G__37807_38217 = self__.buf;
var G__37808_38218 = ((G__37819_38213)?v1.buf:null);
var G__37809_38219 = ((G__37820_38214)?v2.buf:null);
var G__37810_38220 = (G__37807_38217[(0)]);
var G__37811_38221 = (G__37807_38217[(1)]);
var G__37812_38222 = (G__37807_38217[(2)]);
var G__37813_38223 = ((G__37819_38213)?(G__37808_38218[(0)]):(cljs.core.truth_(G__37821_38215)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37814_38224 = ((G__37819_38213)?(G__37808_38218[(1)]):(cljs.core.truth_(G__37821_38215)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37815_38225 = ((G__37819_38213)?(G__37808_38218[(2)]):(cljs.core.truth_(G__37821_38215)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37816_38226 = ((G__37820_38214)?(G__37809_38219[(0)]):(cljs.core.truth_(G__37822_38216)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37817_38227 = ((G__37820_38214)?(G__37809_38219[(1)]):(cljs.core.truth_(G__37822_38216)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37818_38228 = ((G__37820_38214)?(G__37809_38219[(2)]):(cljs.core.truth_(G__37822_38216)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37810_38220 / G__37813_38223) / G__37816_38226));

(dest__18831__auto__[(1)] = ((G__37811_38221 / G__37814_38224) / G__37817_38227));

(dest__18831__auto__[(2)] = ((G__37812_38222 / G__37815_38225) / G__37818_38228));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$div$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37823 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37823[(0)]) / x));

(dest__18809__auto__[(1)] = ((G__37823[(1)]) / y));

(dest__18809__auto__[(2)] = ((G__37823[(2)]) / z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37824_38229 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37825_38230 = v.buf;
(dest__18820__auto__[(0)] = ((G__37824_38229[(0)]) + (G__37825_38230[(0)])));

(dest__18820__auto__[(1)] = ((G__37824_38229[(1)]) + (G__37825_38230[(1)])));

(dest__18820__auto__[(2)] = ((G__37824_38229[(2)]) + (G__37825_38230[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37824_38229[(0)]) + v));

(dest__18820__auto__[(1)] = ((G__37824_38229[(1)]) + v));

(dest__18820__auto__[(2)] = ((G__37824_38229[(2)]) + v));
} else {
(dest__18820__auto__[(0)] = ((G__37824_38229[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37824_38229[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37824_38229[(2)]) + cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37838_38231 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37839_38232 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37840_38233 = ((!(G__37838_38231))?typeof v1 === 'number':null);
var G__37841_38234 = ((!(G__37839_38232))?typeof v2 === 'number':null);
var G__37826_38235 = self__.buf;
var G__37827_38236 = ((G__37838_38231)?v1.buf:null);
var G__37828_38237 = ((G__37839_38232)?v2.buf:null);
var G__37829_38238 = (G__37826_38235[(0)]);
var G__37830_38239 = (G__37826_38235[(1)]);
var G__37831_38240 = (G__37826_38235[(2)]);
var G__37832_38241 = ((G__37838_38231)?(G__37827_38236[(0)]):(cljs.core.truth_(G__37840_38233)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37833_38242 = ((G__37838_38231)?(G__37827_38236[(1)]):(cljs.core.truth_(G__37840_38233)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37834_38243 = ((G__37838_38231)?(G__37827_38236[(2)]):(cljs.core.truth_(G__37840_38233)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37835_38244 = ((G__37839_38232)?(G__37828_38237[(0)]):(cljs.core.truth_(G__37841_38234)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37836_38245 = ((G__37839_38232)?(G__37828_38237[(1)]):(cljs.core.truth_(G__37841_38234)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37837_38246 = ((G__37839_38232)?(G__37828_38237[(2)]):(cljs.core.truth_(G__37841_38234)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37829_38238 + G__37832_38241) + G__37835_38244));

(dest__18831__auto__[(1)] = ((G__37830_38239 + G__37833_38242) + G__37836_38245));

(dest__18831__auto__[(2)] = ((G__37831_38240 + G__37834_38243) + G__37837_38246));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37842 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37842[(0)]) + x));

(dest__18809__auto__[(1)] = ((G__37842[(1)]) + y));

(dest__18809__auto__[(2)] = ((G__37842[(2)]) + z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$abs$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var dest__18803__auto__ = (new Float32Array((3)));
var G__37843_38247 = self__.buf;
(dest__18803__auto__[(0)] = thi.ng.math.core.abs.call(null,(G__37843_38247[(0)])));

(dest__18803__auto__[(1)] = thi.ng.math.core.abs.call(null,(G__37843_38247[(1)])));

(dest__18803__auto__[(2)] = thi.ng.math.core.abs.call(null,(G__37843_38247[(2)])));

return (new thi.ng.geom.core.vector.Vec3(dest__18803__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMathOps$subm$arity$3 = (function (_,a,b){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37856_38248 = (a instanceof thi.ng.geom.core.vector.Vec3);
var G__37857_38249 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37858_38250 = ((!(G__37856_38248))?typeof a === 'number':null);
var G__37859_38251 = ((!(G__37857_38249))?typeof b === 'number':null);
var G__37844_38252 = self__.buf;
var G__37845_38253 = ((G__37856_38248)?a.buf:null);
var G__37846_38254 = ((G__37857_38249)?b.buf:null);
var G__37847_38255 = (G__37844_38252[(0)]);
var G__37848_38256 = (G__37844_38252[(1)]);
var G__37849_38257 = (G__37844_38252[(2)]);
var G__37850_38258 = ((G__37856_38248)?(G__37845_38253[(0)]):(cljs.core.truth_(G__37858_38250)?a:cljs.core.nth.call(null,a,(0),0.0)));
var G__37851_38259 = ((G__37856_38248)?(G__37845_38253[(1)]):(cljs.core.truth_(G__37858_38250)?a:cljs.core.nth.call(null,a,(1),0.0)));
var G__37852_38260 = ((G__37856_38248)?(G__37845_38253[(2)]):(cljs.core.truth_(G__37858_38250)?a:cljs.core.nth.call(null,a,(2),0.0)));
var G__37853_38261 = ((G__37857_38249)?(G__37846_38254[(0)]):(cljs.core.truth_(G__37859_38251)?b:cljs.core.nth.call(null,b,(0),1.0)));
var G__37854_38262 = ((G__37857_38249)?(G__37846_38254[(1)]):(cljs.core.truth_(G__37859_38251)?b:cljs.core.nth.call(null,b,(1),1.0)));
var G__37855_38263 = ((G__37857_38249)?(G__37846_38254[(2)]):(cljs.core.truth_(G__37859_38251)?b:cljs.core.nth.call(null,b,(2),1.0)));
(dest__18831__auto__[(0)] = ((G__37847_38255 - G__37850_38258) * G__37853_38261));

(dest__18831__auto__[(1)] = ((G__37848_38256 - G__37851_38259) * G__37854_38262));

(dest__18831__auto__[(2)] = ((G__37849_38257 - G__37852_38260) * G__37855_38263));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ISeq$_first$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (self__.buf[(0)]);
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ISeq$_rest$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.cons.call(null,(self__.buf[(1)]),cljs.core.cons.call(null,(self__.buf[(2)]),null));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTranslate$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTranslate$translate$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37860_38264 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37861_38265 = v.buf;
(dest__18820__auto__[(0)] = ((G__37860_38264[(0)]) + (G__37861_38265[(0)])));

(dest__18820__auto__[(1)] = ((G__37860_38264[(1)]) + (G__37861_38265[(1)])));

(dest__18820__auto__[(2)] = ((G__37860_38264[(2)]) + (G__37861_38265[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37860_38264[(0)]) + v));

(dest__18820__auto__[(1)] = ((G__37860_38264[(1)]) + v));

(dest__18820__auto__[(2)] = ((G__37860_38264[(2)]) + v));
} else {
(dest__18820__auto__[(0)] = ((G__37860_38264[(0)]) + cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37860_38264[(1)]) + cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37860_38264[(2)]) + cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTranslate$translate$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37874_38266 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37875_38267 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37876_38268 = ((!(G__37874_38266))?typeof v1 === 'number':null);
var G__37877_38269 = ((!(G__37875_38267))?typeof v2 === 'number':null);
var G__37862_38270 = self__.buf;
var G__37863_38271 = ((G__37874_38266)?v1.buf:null);
var G__37864_38272 = ((G__37875_38267)?v2.buf:null);
var G__37865_38273 = (G__37862_38270[(0)]);
var G__37866_38274 = (G__37862_38270[(1)]);
var G__37867_38275 = (G__37862_38270[(2)]);
var G__37868_38276 = ((G__37874_38266)?(G__37863_38271[(0)]):(cljs.core.truth_(G__37876_38268)?v1:cljs.core.nth.call(null,v1,(0),0.0)));
var G__37869_38277 = ((G__37874_38266)?(G__37863_38271[(1)]):(cljs.core.truth_(G__37876_38268)?v1:cljs.core.nth.call(null,v1,(1),0.0)));
var G__37870_38278 = ((G__37874_38266)?(G__37863_38271[(2)]):(cljs.core.truth_(G__37876_38268)?v1:cljs.core.nth.call(null,v1,(2),0.0)));
var G__37871_38279 = ((G__37875_38267)?(G__37864_38272[(0)]):(cljs.core.truth_(G__37877_38269)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37872_38280 = ((G__37875_38267)?(G__37864_38272[(1)]):(cljs.core.truth_(G__37877_38269)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37873_38281 = ((G__37875_38267)?(G__37864_38272[(2)]):(cljs.core.truth_(G__37877_38269)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = ((G__37865_38273 + G__37868_38276) + G__37871_38279));

(dest__18831__auto__[(1)] = ((G__37866_38274 + G__37869_38277) + G__37872_38280));

(dest__18831__auto__[(2)] = ((G__37867_38275 + G__37870_38278) + G__37873_38281));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PTranslate$translate$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37878 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37878[(0)]) + x));

(dest__18809__auto__[(1)] = ((G__37878[(1)]) + y));

(dest__18809__auto__[(2)] = ((G__37878[(2)]) + z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IAssociative$_contains_key_QMARK_$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
if(typeof k === 'number'){
return ((k >= (0))) && ((k <= (2)));
} else {
if(cljs.core.truth_(thi.ng.geom.core.vector.swizzle3_fns.call(null,k))){
return true;
} else {
return false;
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IAssociative$_assoc$arity$3 = (function (_,k,v){
var self__ = this;
var ___$1 = this;
if(typeof k === 'number'){
if(((k >= (0))) && ((k <= (2)))){
var b = (new Float32Array(self__.buf));
(b[k] = v);

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
} else {
if((k === (3))){
return cljs.core.conj.call(null,___$1,v);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
} else {
if((k instanceof cljs.core.Keyword)){
if(cljs.core._EQ_.call(null,new cljs.core.Keyword(null,"w","w",354169001),k)){
return cljs.core.conj.call(null,___$1,v);
} else {
return (new thi.ng.geom.core.vector.Vec3(thi.ng.geom.core.vector.swizzle_assoc_STAR_.call(null,self__.buf,(new Float32Array(self__.buf)),new cljs.core.PersistentArrayMap(null, 3, ["x",(0),"y",(1),"z",(2)], null),k,v),null,self__._meta));
}
} else {
return null;
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ISeqable$_seq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PScale$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PScale$scale$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37879_38282 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37880_38283 = v.buf;
(dest__18820__auto__[(0)] = ((G__37879_38282[(0)]) * (G__37880_38283[(0)])));

(dest__18820__auto__[(1)] = ((G__37879_38282[(1)]) * (G__37880_38283[(1)])));

(dest__18820__auto__[(2)] = ((G__37879_38282[(2)]) * (G__37880_38283[(2)])));
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = ((G__37879_38282[(0)]) * v));

(dest__18820__auto__[(1)] = ((G__37879_38282[(1)]) * v));

(dest__18820__auto__[(2)] = ((G__37879_38282[(2)]) * v));
} else {
(dest__18820__auto__[(0)] = ((G__37879_38282[(0)]) * cljs.core.nth.call(null,v,(0),0.0)));

(dest__18820__auto__[(1)] = ((G__37879_38282[(1)]) * cljs.core.nth.call(null,v,(1),0.0)));

(dest__18820__auto__[(2)] = ((G__37879_38282[(2)]) * cljs.core.nth.call(null,v,(2),0.0)));
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PScale$scale$arity$3 = (function (_,v1,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37893_38284 = (v1 instanceof thi.ng.geom.core.vector.Vec3);
var G__37894_38285 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37895_38286 = ((!(G__37893_38284))?typeof v1 === 'number':null);
var G__37896_38287 = ((!(G__37894_38285))?typeof v2 === 'number':null);
var G__37881_38288 = self__.buf;
var G__37882_38289 = ((G__37893_38284)?v1.buf:null);
var G__37883_38290 = ((G__37894_38285)?v2.buf:null);
var G__37884_38291 = (G__37881_38288[(0)]);
var G__37885_38292 = (G__37881_38288[(1)]);
var G__37886_38293 = (G__37881_38288[(2)]);
var G__37887_38294 = ((G__37893_38284)?(G__37882_38289[(0)]):(cljs.core.truth_(G__37895_38286)?v1:cljs.core.nth.call(null,v1,(0),1.0)));
var G__37888_38295 = ((G__37893_38284)?(G__37882_38289[(1)]):(cljs.core.truth_(G__37895_38286)?v1:cljs.core.nth.call(null,v1,(1),1.0)));
var G__37889_38296 = ((G__37893_38284)?(G__37882_38289[(2)]):(cljs.core.truth_(G__37895_38286)?v1:cljs.core.nth.call(null,v1,(2),1.0)));
var G__37890_38297 = ((G__37894_38285)?(G__37883_38290[(0)]):(cljs.core.truth_(G__37896_38287)?v2:cljs.core.nth.call(null,v2,(0),1.0)));
var G__37891_38298 = ((G__37894_38285)?(G__37883_38290[(1)]):(cljs.core.truth_(G__37896_38287)?v2:cljs.core.nth.call(null,v2,(1),1.0)));
var G__37892_38299 = ((G__37894_38285)?(G__37883_38290[(2)]):(cljs.core.truth_(G__37896_38287)?v2:cljs.core.nth.call(null,v2,(2),1.0)));
(dest__18831__auto__[(0)] = ((G__37884_38291 * G__37887_38294) * G__37890_38297));

(dest__18831__auto__[(1)] = ((G__37885_38292 * G__37888_38295) * G__37891_38298));

(dest__18831__auto__[(2)] = ((G__37886_38293 * G__37889_38296) * G__37892_38299));

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PScale$scale$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
var G__37897 = self__.buf;
var dest__18809__auto__ = (new Float32Array((3)));
(dest__18809__auto__[(0)] = ((G__37897[(0)]) * x));

(dest__18809__auto__[(1)] = ((G__37897[(1)]) * y));

(dest__18809__auto__[(2)] = ((G__37897[(2)]) * z));

return (new thi.ng.geom.core.vector.Vec3(dest__18809__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.vector.Vec3((new Float32Array(self__.buf)),self__._hash,m));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$ICollection$_conj$arity$2 = (function (_,x){
var self__ = this;
var ___$1 = this;
return cljs.core.with_meta.call(null,new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [(self__.buf[(0)]),(self__.buf[(1)]),(self__.buf[(2)]),x], null),self__._meta);
});

thi.ng.geom.core.vector.Vec3.prototype.call = (function() {
var G__38300 = null;
var G__38300__2 = (function (self__,k){
var self__ = this;
var self____$1 = this;
var _ = self____$1;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});
var G__38300__3 = (function (self__,k,nf){
var self__ = this;
var self____$1 = this;
var _ = self____$1;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return nf;
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return nf;
}
}
});
G__38300 = function(self__,k,nf){
switch(arguments.length){
case 2:
return G__38300__2.call(this,self__,k);
case 3:
return G__38300__3.call(this,self__,k,nf);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
G__38300.cljs$core$IFn$_invoke$arity$2 = G__38300__2;
G__38300.cljs$core$IFn$_invoke$arity$3 = G__38300__3;
return G__38300;
})()
;

thi.ng.geom.core.vector.Vec3.prototype.apply = (function (self__,args37526){
var self__ = this;
var self____$1 = this;
return self____$1.call.apply(self____$1,[self____$1].concat(cljs.core.aclone.call(null,args37526)));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IFn$_invoke$arity$1 = (function (k){
var self__ = this;
var _ = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IFn$_invoke$arity$2 = (function (k,nf){
var self__ = this;
var _ = this;
if((k instanceof cljs.core.Keyword)){
var temp__4423__auto__ = thi.ng.geom.core.vector.swizzle3_fns.call(null,k);
if(cljs.core.truth_(temp__4423__auto__)){
var f = temp__4423__auto__;
return f.call(null,_);
} else {
return nf;
}
} else {
if(((k >= (0))) && ((k <= (2)))){
return (self__.buf[k]);
} else {
return nf;
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMinMax$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMinMax$min$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37898_38301 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37899_38302 = v.buf;
(dest__18820__auto__[(0)] = (function (){var a__18454__auto__ = (G__37898_38301[(0)]);
var b__18455__auto__ = (G__37899_38302[(0)]);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18454__auto__ = (G__37898_38301[(1)]);
var b__18455__auto__ = (G__37899_38302[(1)]);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18454__auto__ = (G__37898_38301[(2)]);
var b__18455__auto__ = (G__37899_38302[(2)]);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = (function (){var a__18454__auto__ = (G__37898_38301[(0)]);
var b__18455__auto__ = v;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18454__auto__ = (G__37898_38301[(1)]);
var b__18455__auto__ = v;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18454__auto__ = (G__37898_38301[(2)]);
var b__18455__auto__ = v;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
} else {
(dest__18820__auto__[(0)] = (function (){var a__18454__auto__ = (G__37898_38301[(0)]);
var b__18455__auto__ = cljs.core.nth.call(null,v,(0),0.0);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18454__auto__ = (G__37898_38301[(1)]);
var b__18455__auto__ = cljs.core.nth.call(null,v,(1),0.0);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18454__auto__ = (G__37898_38301[(2)]);
var b__18455__auto__ = cljs.core.nth.call(null,v,(2),0.0);
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMinMax$min$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37912_38303 = (v instanceof thi.ng.geom.core.vector.Vec3);
var G__37913_38304 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37914_38305 = ((!(G__37912_38303))?typeof v === 'number':null);
var G__37915_38306 = ((!(G__37913_38304))?typeof v2 === 'number':null);
var G__37900_38307 = self__.buf;
var G__37901_38308 = ((G__37912_38303)?v.buf:null);
var G__37902_38309 = ((G__37913_38304)?v2.buf:null);
var G__37903_38310 = (G__37900_38307[(0)]);
var G__37904_38311 = (G__37900_38307[(1)]);
var G__37905_38312 = (G__37900_38307[(2)]);
var G__37906_38313 = ((G__37912_38303)?(G__37901_38308[(0)]):(cljs.core.truth_(G__37914_38305)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37907_38314 = ((G__37912_38303)?(G__37901_38308[(1)]):(cljs.core.truth_(G__37914_38305)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37908_38315 = ((G__37912_38303)?(G__37901_38308[(2)]):(cljs.core.truth_(G__37914_38305)?v:cljs.core.nth.call(null,v,(2),0.0)));
var G__37909_38316 = ((G__37913_38304)?(G__37902_38309[(0)]):(cljs.core.truth_(G__37915_38306)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37910_38317 = ((G__37913_38304)?(G__37902_38309[(1)]):(cljs.core.truth_(G__37915_38306)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37911_38318 = ((G__37913_38304)?(G__37902_38309[(2)]):(cljs.core.truth_(G__37915_38306)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = (function (){var a__18454__auto__ = (function (){var a__18454__auto__ = G__37903_38310;
var b__18455__auto__ = G__37906_38313;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})();
var b__18455__auto__ = G__37909_38316;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18831__auto__[(1)] = (function (){var a__18454__auto__ = (function (){var a__18454__auto__ = G__37904_38311;
var b__18455__auto__ = G__37907_38314;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})();
var b__18455__auto__ = G__37910_38317;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

(dest__18831__auto__[(2)] = (function (){var a__18454__auto__ = (function (){var a__18454__auto__ = G__37905_38312;
var b__18455__auto__ = G__37908_38315;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})();
var b__18455__auto__ = G__37911_38318;
if((a__18454__auto__ <= b__18455__auto__)){
return a__18454__auto__;
} else {
return b__18455__auto__;
}
})());

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMinMax$max$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var dest__18820__auto__ = (new Float32Array((3)));
var G__37916_38319 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37917_38320 = v.buf;
(dest__18820__auto__[(0)] = (function (){var a__18461__auto__ = (G__37916_38319[(0)]);
var b__18462__auto__ = (G__37917_38320[(0)]);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18461__auto__ = (G__37916_38319[(1)]);
var b__18462__auto__ = (G__37917_38320[(1)]);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18461__auto__ = (G__37916_38319[(2)]);
var b__18462__auto__ = (G__37917_38320[(2)]);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
} else {
if(typeof v === 'number'){
(dest__18820__auto__[(0)] = (function (){var a__18461__auto__ = (G__37916_38319[(0)]);
var b__18462__auto__ = v;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18461__auto__ = (G__37916_38319[(1)]);
var b__18462__auto__ = v;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18461__auto__ = (G__37916_38319[(2)]);
var b__18462__auto__ = v;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
} else {
(dest__18820__auto__[(0)] = (function (){var a__18461__auto__ = (G__37916_38319[(0)]);
var b__18462__auto__ = cljs.core.nth.call(null,v,(0),0.0);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(1)] = (function (){var a__18461__auto__ = (G__37916_38319[(1)]);
var b__18462__auto__ = cljs.core.nth.call(null,v,(1),0.0);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18820__auto__[(2)] = (function (){var a__18461__auto__ = (G__37916_38319[(2)]);
var b__18462__auto__ = cljs.core.nth.call(null,v,(2),0.0);
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());
}
}

return (new thi.ng.geom.core.vector.Vec3(dest__18820__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMinMax$max$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var dest__18831__auto__ = (new Float32Array((3)));
var G__37930_38321 = (v instanceof thi.ng.geom.core.vector.Vec3);
var G__37931_38322 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37932_38323 = ((!(G__37930_38321))?typeof v === 'number':null);
var G__37933_38324 = ((!(G__37931_38322))?typeof v2 === 'number':null);
var G__37918_38325 = self__.buf;
var G__37919_38326 = ((G__37930_38321)?v.buf:null);
var G__37920_38327 = ((G__37931_38322)?v2.buf:null);
var G__37921_38328 = (G__37918_38325[(0)]);
var G__37922_38329 = (G__37918_38325[(1)]);
var G__37923_38330 = (G__37918_38325[(2)]);
var G__37924_38331 = ((G__37930_38321)?(G__37919_38326[(0)]):(cljs.core.truth_(G__37932_38323)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37925_38332 = ((G__37930_38321)?(G__37919_38326[(1)]):(cljs.core.truth_(G__37932_38323)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37926_38333 = ((G__37930_38321)?(G__37919_38326[(2)]):(cljs.core.truth_(G__37932_38323)?v:cljs.core.nth.call(null,v,(2),0.0)));
var G__37927_38334 = ((G__37931_38322)?(G__37920_38327[(0)]):(cljs.core.truth_(G__37933_38324)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37928_38335 = ((G__37931_38322)?(G__37920_38327[(1)]):(cljs.core.truth_(G__37933_38324)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37929_38336 = ((G__37931_38322)?(G__37920_38327[(2)]):(cljs.core.truth_(G__37933_38324)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(dest__18831__auto__[(0)] = (function (){var a__18461__auto__ = (function (){var a__18461__auto__ = G__37921_38328;
var b__18462__auto__ = G__37924_38331;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})();
var b__18462__auto__ = G__37927_38334;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18831__auto__[(1)] = (function (){var a__18461__auto__ = (function (){var a__18461__auto__ = G__37922_38329;
var b__18462__auto__ = G__37925_38332;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})();
var b__18462__auto__ = G__37928_38335;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

(dest__18831__auto__[(2)] = (function (){var a__18461__auto__ = (function (){var a__18461__auto__ = G__37923_38330;
var b__18462__auto__ = G__37926_38333;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})();
var b__18462__auto__ = G__37929_38336;
if((a__18461__auto__ >= b__18462__auto__)){
return a__18461__auto__;
} else {
return b__18462__auto__;
}
})());

return (new thi.ng.geom.core.vector.Vec3(dest__18831__auto__,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.cljs$core$IComparable$_compare$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
if((o instanceof thi.ng.geom.core.vector.Vec3)){
var b_SINGLEQUOTE_ = o.buf;
var c = cljs.core.compare.call(null,(self__.buf[(0)]),(b_SINGLEQUOTE_[(0)]));
if(((0) === c)){
var c__$1 = cljs.core.compare.call(null,(self__.buf[(1)]),(b_SINGLEQUOTE_[(1)]));
if(((0) === c__$1)){
return cljs.core.compare.call(null,(self__.buf[(2)]),(b_SINGLEQUOTE_[(2)]));
} else {
return c__$1;
}
} else {
return c;
}
} else {
var c = cljs.core.count.call(null,o);
if(((3) === c)){
return (- cljs.core.compare.call(null,o,___$1));
} else {
return ((3) - c);
}
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PVectorReduce$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PVectorReduce$reduce_vector$arity$3 = (function (_,f,xs){
var self__ = this;
var ___$1 = this;
var buf_SINGLEQUOTE_ = (new Float32Array(self__.buf));
return (new thi.ng.geom.core.vector.Vec3(thi.ng.geom.core.vector.vec3_reduce_STAR_.call(null,f,buf_SINGLEQUOTE_,xs),null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PVectorReduce$reduce_vector$arity$4 = (function (_,f,f2,xs){
var self__ = this;
var ___$1 = this;
var buf_SINGLEQUOTE_ = (new Float32Array(self__.buf));
thi.ng.geom.core.vector.vec3_reduce_STAR_.call(null,f,buf_SINGLEQUOTE_,xs);

(buf_SINGLEQUOTE_[(0)] = f2.call(null,(buf_SINGLEQUOTE_[(0)]),(0)));

(buf_SINGLEQUOTE_[(1)] = f2.call(null,(buf_SINGLEQUOTE_[(1)]),(1)));

(buf_SINGLEQUOTE_[(2)] = f2.call(null,(buf_SINGLEQUOTE_[(2)]),(2)));

return (new thi.ng.geom.core.vector.Vec3(buf_SINGLEQUOTE_,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$math$core$PDeltaEquals$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,___$1,v,thi.ng.math.core._STAR_eps_STAR_);
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$3 = (function (_,v,eps){
var self__ = this;
var ___$1 = this;
if(cljs.core.sequential_QMARK_.call(null,v)){
if(((3) === cljs.core.count.call(null,v))){
var G__37934 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37935 = v.buf;
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37934[(0)]),(G__37935[(0)]),eps))){
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37934[(1)]),(G__37935[(1)]),eps))){
return thi.ng.math.core.delta_EQ_.call(null,(G__37934[(2)]),(G__37935[(2)]),eps);
} else {
return null;
}
} else {
return null;
}
} else {
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37934[(0)]),cljs.core.nth.call(null,v,(0),0.0),eps))){
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,(G__37934[(1)]),cljs.core.nth.call(null,v,(1),0.0),eps))){
return thi.ng.math.core.delta_EQ_.call(null,(G__37934[(2)]),cljs.core.nth.call(null,v,(2),0.0),eps);
} else {
return null;
}
} else {
return null;
}
}
} else {
return null;
}
} else {
return null;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMagnitude$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMagnitude$mag$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__37936 = self__.buf;
var G__37937 = (G__37936[(0)]);
var G__37938 = (G__37936[(1)]);
var G__37939 = (G__37936[(2)]);
return Math.sqrt((((G__37937 * G__37937) + (G__37938 * G__37938)) + (G__37939 * G__37939)));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PMagnitude$mag_squared$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var G__37940 = self__.buf;
var G__37941 = (G__37940[(0)]);
var G__37942 = (G__37940[(1)]);
var G__37943 = (G__37940[(2)]);
return (((G__37941 * G__37941) + (G__37942 * G__37942)) + (G__37943 * G__37943));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInterpolate$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInterpolate$mix$arity$2 = (function (_,v){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((3)));
var G__37944_38337 = self__.buf;
if((v instanceof thi.ng.geom.core.vector.Vec3)){
var G__37945_38338 = v.buf;
(b[(0)] = (((G__37944_38337[(0)]) + (G__37945_38338[(0)])) * 0.5));

(b[(1)] = (((G__37944_38337[(1)]) + (G__37945_38338[(1)])) * 0.5));

(b[(2)] = (((G__37944_38337[(2)]) + (G__37945_38338[(2)])) * 0.5));
} else {
if(typeof v === 'number'){
(b[(0)] = (((G__37944_38337[(0)]) + v) * 0.5));

(b[(1)] = (((G__37944_38337[(1)]) + v) * 0.5));

(b[(2)] = (((G__37944_38337[(2)]) + v) * 0.5));
} else {
(b[(0)] = (((G__37944_38337[(0)]) + cljs.core.nth.call(null,v,(0),0.0)) * 0.5));

(b[(1)] = (((G__37944_38337[(1)]) + cljs.core.nth.call(null,v,(1),0.0)) * 0.5));

(b[(2)] = (((G__37944_38337[(2)]) + cljs.core.nth.call(null,v,(2),0.0)) * 0.5));
}
}

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInterpolate$mix$arity$3 = (function (_,v,v2){
var self__ = this;
var ___$1 = this;
var b = (new Float32Array((3)));
var G__37958_38339 = (v instanceof thi.ng.geom.core.vector.Vec3);
var G__37959_38340 = (v2 instanceof thi.ng.geom.core.vector.Vec3);
var G__37960_38341 = ((!(G__37958_38339))?typeof v === 'number':null);
var G__37961_38342 = ((!(G__37959_38340))?typeof v2 === 'number':null);
var G__37946_38343 = self__.buf;
var G__37947_38344 = ((G__37958_38339)?v.buf:null);
var G__37948_38345 = ((G__37959_38340)?v2.buf:null);
var G__37949_38346 = (G__37946_38343[(0)]);
var G__37950_38347 = (G__37946_38343[(1)]);
var G__37951_38348 = (G__37946_38343[(2)]);
var G__37952_38349 = ((G__37958_38339)?(G__37947_38344[(0)]):(cljs.core.truth_(G__37960_38341)?v:cljs.core.nth.call(null,v,(0),0.0)));
var G__37953_38350 = ((G__37958_38339)?(G__37947_38344[(1)]):(cljs.core.truth_(G__37960_38341)?v:cljs.core.nth.call(null,v,(1),0.0)));
var G__37954_38351 = ((G__37958_38339)?(G__37947_38344[(2)]):(cljs.core.truth_(G__37960_38341)?v:cljs.core.nth.call(null,v,(2),0.0)));
var G__37955_38352 = ((G__37959_38340)?(G__37948_38345[(0)]):(cljs.core.truth_(G__37961_38342)?v2:cljs.core.nth.call(null,v2,(0),0.0)));
var G__37956_38353 = ((G__37959_38340)?(G__37948_38345[(1)]):(cljs.core.truth_(G__37961_38342)?v2:cljs.core.nth.call(null,v2,(1),0.0)));
var G__37957_38354 = ((G__37959_38340)?(G__37948_38345[(2)]):(cljs.core.truth_(G__37961_38342)?v2:cljs.core.nth.call(null,v2,(2),0.0)));
(b[(0)] = (((G__37952_38349 - G__37949_38346) * G__37955_38352) + G__37949_38346));

(b[(1)] = (((G__37953_38350 - G__37950_38347) * G__37956_38353) + G__37950_38347));

(b[(2)] = (((G__37954_38351 - G__37951_38348) * G__37957_38354) + G__37951_38348));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PInterpolate$mix$arity$6 = (function (_,b,c,d,u,v){
var self__ = this;
var ___$1 = this;
var b_SINGLEQUOTE_ = (new Float32Array((3)));
var dv_QMARK_ = (d instanceof thi.ng.geom.core.vector.Vec3);
var dn_QMARK_ = typeof d === 'number';
var dv = ((dv_QMARK_)?d.buf:null);
var dx = ((dv_QMARK_)?(dv[(0)]):((dn_QMARK_)?d:cljs.core.nth.call(null,d,(0),0.0)));
var dy = ((dv_QMARK_)?(dv[(1)]):((dn_QMARK_)?d:cljs.core.nth.call(null,d,(1),0.0)));
var dz = ((dv_QMARK_)?(dv[(2)]):((dn_QMARK_)?d:cljs.core.nth.call(null,d,(2),0.0)));
var G__37974_38355 = (b instanceof thi.ng.geom.core.vector.Vec3);
var G__37975_38356 = (c instanceof thi.ng.geom.core.vector.Vec3);
var G__37976_38357 = ((!(G__37974_38355))?typeof b === 'number':null);
var G__37977_38358 = ((!(G__37975_38356))?typeof c === 'number':null);
var G__37962_38359 = self__.buf;
var G__37963_38360 = ((G__37974_38355)?b.buf:null);
var G__37964_38361 = ((G__37975_38356)?c.buf:null);
var G__37965_38362 = (G__37962_38359[(0)]);
var G__37966_38363 = (G__37962_38359[(1)]);
var G__37967_38364 = (G__37962_38359[(2)]);
var G__37968_38365 = ((G__37974_38355)?(G__37963_38360[(0)]):(cljs.core.truth_(G__37976_38357)?b:cljs.core.nth.call(null,b,(0),0.0)));
var G__37969_38366 = ((G__37974_38355)?(G__37963_38360[(1)]):(cljs.core.truth_(G__37976_38357)?b:cljs.core.nth.call(null,b,(1),0.0)));
var G__37970_38367 = ((G__37974_38355)?(G__37963_38360[(2)]):(cljs.core.truth_(G__37976_38357)?b:cljs.core.nth.call(null,b,(2),0.0)));
var G__37971_38368 = ((G__37975_38356)?(G__37964_38361[(0)]):(cljs.core.truth_(G__37977_38358)?c:cljs.core.nth.call(null,c,(0),0.0)));
var G__37972_38369 = ((G__37975_38356)?(G__37964_38361[(1)]):(cljs.core.truth_(G__37977_38358)?c:cljs.core.nth.call(null,c,(1),0.0)));
var G__37973_38370 = ((G__37975_38356)?(G__37964_38361[(2)]):(cljs.core.truth_(G__37977_38358)?c:cljs.core.nth.call(null,c,(2),0.0)));
var x1_38371 = (((G__37968_38365 - G__37965_38362) * u) + G__37965_38362);
var y1_38372 = (((G__37969_38366 - G__37966_38363) * u) + G__37966_38363);
var z1_38373 = (((G__37970_38367 - G__37967_38364) * u) + G__37967_38364);
(b_SINGLEQUOTE_[(0)] = ((((((dx - G__37971_38368) * u) + G__37971_38368) - x1_38371) * v) + x1_38371));

(b_SINGLEQUOTE_[(1)] = ((((((dy - G__37972_38369) * u) + G__37972_38369) - y1_38372) * v) + y1_38372));

(b_SINGLEQUOTE_[(2)] = ((((((dz - G__37973_38370) * u) + G__37973_38370) - z1_38373) * v) + z1_38373));

return (new thi.ng.geom.core.vector.Vec3(b_SINGLEQUOTE_,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PLimit$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PLimit$limit$arity$2 = (function (_,len){
var self__ = this;
var ___$1 = this;
if((thi.ng.geom.core.mag_squared.call(null,___$1) > (len * len))){
return thi.ng.geom.core.normalize.call(null,___$1,len);
} else {
return ___$1;
}
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PPolar$ = true;

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PPolar$as_polar$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var r = thi.ng.geom.core.mag.call(null,___$1);
var b = (new Float32Array((3)));
(b[(0)] = r);

(b[(1)] = Math.asin(((self__.buf[(2)]) / r)));

(b[(2)] = Math.atan2((self__.buf[(1)]),(self__.buf[(0)])));

return (new thi.ng.geom.core.vector.Vec3(b,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.prototype.thi$ng$geom$core$PPolar$as_cartesian$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var b = self__.buf;
var x = (b[(0)]);
var y = (b[(1)]);
var z = (b[(2)]);
var rcos = (x * Math.cos(y));
var b_SINGLEQUOTE_ = (new Float32Array((3)));
(b_SINGLEQUOTE_[(0)] = (rcos * Math.cos(z)));

(b_SINGLEQUOTE_[(1)] = (rcos * Math.sin(z)));

(b_SINGLEQUOTE_[(2)] = (x * Math.sin(y)));

return (new thi.ng.geom.core.vector.Vec3(b_SINGLEQUOTE_,null,self__._meta));
});

thi.ng.geom.core.vector.Vec3.getBasis = (function (){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"buf","buf",1426618187,null),new cljs.core.Symbol(null,"_hash","_hash",-2130838312,null),new cljs.core.Symbol(null,"_meta","_meta",-1716892533,null)], null);
});

thi.ng.geom.core.vector.Vec3.cljs$lang$type = true;

thi.ng.geom.core.vector.Vec3.cljs$lang$ctorStr = "thi.ng.geom.core.vector/Vec3";

thi.ng.geom.core.vector.Vec3.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"thi.ng.geom.core.vector/Vec3");
});

thi.ng.geom.core.vector.__GT_Vec3 = (function thi$ng$geom$core$vector$__GT_Vec3(buf,_hash,_meta){
return (new thi.ng.geom.core.vector.Vec3(buf,_hash,_meta));
});

thi.ng.geom.core.vector.x = (function thi$ng$geom$core$vector$x(G__38376){
var G__38374 = (((G__38376 instanceof thi.ng.geom.core.vector.Vec2))?G__38376.buf:G__38376.buf);
return (G__38374[(0)]);
});
thi.ng.geom.core.vector.xx = (function thi$ng$geom$core$vector$xx(G__38379){
var G__38377 = (((G__38379 instanceof thi.ng.geom.core.vector.Vec2))?G__38379.buf:G__38379.buf);
var G__38378 = (new Float32Array(2));
(G__38378[(0)] = (G__38377[(0)]));

(G__38378[(1)] = (G__38377[(0)]));

return (new thi.ng.geom.core.vector.Vec2(G__38378,null,cljs.core.meta.call(null,G__38379)));
});
thi.ng.geom.core.vector.xxx = (function thi$ng$geom$core$vector$xxx(G__38382){
var G__38380 = (((G__38382 instanceof thi.ng.geom.core.vector.Vec2))?G__38382.buf:G__38382.buf);
var G__38381 = (new Float32Array(3));
(G__38381[(0)] = (G__38380[(0)]));

(G__38381[(1)] = (G__38380[(0)]));

(G__38381[(2)] = (G__38380[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38381,null,cljs.core.meta.call(null,G__38382)));
});
thi.ng.geom.core.vector.xxy = (function thi$ng$geom$core$vector$xxy(G__38385){
var G__38383 = (((G__38385 instanceof thi.ng.geom.core.vector.Vec2))?G__38385.buf:G__38385.buf);
var G__38384 = (new Float32Array(3));
(G__38384[(0)] = (G__38383[(0)]));

(G__38384[(1)] = (G__38383[(0)]));

(G__38384[(2)] = (G__38383[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38384,null,cljs.core.meta.call(null,G__38385)));
});
thi.ng.geom.core.vector.xxz = (function thi$ng$geom$core$vector$xxz(G__38388){
if((G__38388 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38388","G__38388",1787717164,null))))].join('')));
}

var G__38386 = (((G__38388 instanceof thi.ng.geom.core.vector.Vec2))?G__38388.buf:G__38388.buf);
var G__38387 = (new Float32Array(3));
(G__38387[(0)] = (G__38386[(0)]));

(G__38387[(1)] = (G__38386[(0)]));

(G__38387[(2)] = (G__38386[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38387,null,cljs.core.meta.call(null,G__38388)));
});
thi.ng.geom.core.vector.xy = (function thi$ng$geom$core$vector$xy(G__38391){
var G__38389 = (((G__38391 instanceof thi.ng.geom.core.vector.Vec2))?G__38391.buf:G__38391.buf);
var G__38390 = (new Float32Array(2));
(G__38390[(0)] = (G__38389[(0)]));

(G__38390[(1)] = (G__38389[(1)]));

return (new thi.ng.geom.core.vector.Vec2(G__38390,null,cljs.core.meta.call(null,G__38391)));
});
thi.ng.geom.core.vector.xyx = (function thi$ng$geom$core$vector$xyx(G__38394){
var G__38392 = (((G__38394 instanceof thi.ng.geom.core.vector.Vec2))?G__38394.buf:G__38394.buf);
var G__38393 = (new Float32Array(3));
(G__38393[(0)] = (G__38392[(0)]));

(G__38393[(1)] = (G__38392[(1)]));

(G__38393[(2)] = (G__38392[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38393,null,cljs.core.meta.call(null,G__38394)));
});
thi.ng.geom.core.vector.xyy = (function thi$ng$geom$core$vector$xyy(G__38397){
var G__38395 = (((G__38397 instanceof thi.ng.geom.core.vector.Vec2))?G__38397.buf:G__38397.buf);
var G__38396 = (new Float32Array(3));
(G__38396[(0)] = (G__38395[(0)]));

(G__38396[(1)] = (G__38395[(1)]));

(G__38396[(2)] = (G__38395[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38396,null,cljs.core.meta.call(null,G__38397)));
});
thi.ng.geom.core.vector.xyz = (function thi$ng$geom$core$vector$xyz(G__38400){
if((G__38400 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38400","G__38400",-2141534967,null))))].join('')));
}

var G__38398 = (((G__38400 instanceof thi.ng.geom.core.vector.Vec2))?G__38400.buf:G__38400.buf);
var G__38399 = (new Float32Array(3));
(G__38399[(0)] = (G__38398[(0)]));

(G__38399[(1)] = (G__38398[(1)]));

(G__38399[(2)] = (G__38398[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38399,null,cljs.core.meta.call(null,G__38400)));
});
thi.ng.geom.core.vector.xz = (function thi$ng$geom$core$vector$xz(G__38403){
if((G__38403 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38403","G__38403",-2058853871,null))))].join('')));
}

var G__38401 = (((G__38403 instanceof thi.ng.geom.core.vector.Vec2))?G__38403.buf:G__38403.buf);
var G__38402 = (new Float32Array(2));
(G__38402[(0)] = (G__38401[(0)]));

(G__38402[(1)] = (G__38401[(2)]));

return (new thi.ng.geom.core.vector.Vec2(G__38402,null,cljs.core.meta.call(null,G__38403)));
});
thi.ng.geom.core.vector.xzx = (function thi$ng$geom$core$vector$xzx(G__38406){
if((G__38406 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38406","G__38406",1453452672,null))))].join('')));
}

var G__38404 = (((G__38406 instanceof thi.ng.geom.core.vector.Vec2))?G__38406.buf:G__38406.buf);
var G__38405 = (new Float32Array(3));
(G__38405[(0)] = (G__38404[(0)]));

(G__38405[(1)] = (G__38404[(2)]));

(G__38405[(2)] = (G__38404[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38405,null,cljs.core.meta.call(null,G__38406)));
});
thi.ng.geom.core.vector.xzy = (function thi$ng$geom$core$vector$xzy(G__38409){
if((G__38409 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38409","G__38409",1669739031,null))))].join('')));
}

var G__38407 = (((G__38409 instanceof thi.ng.geom.core.vector.Vec2))?G__38409.buf:G__38409.buf);
var G__38408 = (new Float32Array(3));
(G__38408[(0)] = (G__38407[(0)]));

(G__38408[(1)] = (G__38407[(2)]));

(G__38408[(2)] = (G__38407[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38408,null,cljs.core.meta.call(null,G__38409)));
});
thi.ng.geom.core.vector.xzz = (function thi$ng$geom$core$vector$xzz(G__38412){
if((G__38412 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38412","G__38412",-496292938,null))))].join('')));
}

var G__38410 = (((G__38412 instanceof thi.ng.geom.core.vector.Vec2))?G__38412.buf:G__38412.buf);
var G__38411 = (new Float32Array(3));
(G__38411[(0)] = (G__38410[(0)]));

(G__38411[(1)] = (G__38410[(2)]));

(G__38411[(2)] = (G__38410[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38411,null,cljs.core.meta.call(null,G__38412)));
});
thi.ng.geom.core.vector.y = (function thi$ng$geom$core$vector$y(G__38415){
var G__38413 = (((G__38415 instanceof thi.ng.geom.core.vector.Vec2))?G__38415.buf:G__38415.buf);
return (G__38413[(1)]);
});
thi.ng.geom.core.vector.yx = (function thi$ng$geom$core$vector$yx(G__38418){
var G__38416 = (((G__38418 instanceof thi.ng.geom.core.vector.Vec2))?G__38418.buf:G__38418.buf);
var G__38417 = (new Float32Array(2));
(G__38417[(0)] = (G__38416[(1)]));

(G__38417[(1)] = (G__38416[(0)]));

return (new thi.ng.geom.core.vector.Vec2(G__38417,null,cljs.core.meta.call(null,G__38418)));
});
thi.ng.geom.core.vector.yxx = (function thi$ng$geom$core$vector$yxx(G__38421){
var G__38419 = (((G__38421 instanceof thi.ng.geom.core.vector.Vec2))?G__38421.buf:G__38421.buf);
var G__38420 = (new Float32Array(3));
(G__38420[(0)] = (G__38419[(1)]));

(G__38420[(1)] = (G__38419[(0)]));

(G__38420[(2)] = (G__38419[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38420,null,cljs.core.meta.call(null,G__38421)));
});
thi.ng.geom.core.vector.yxy = (function thi$ng$geom$core$vector$yxy(G__38424){
var G__38422 = (((G__38424 instanceof thi.ng.geom.core.vector.Vec2))?G__38424.buf:G__38424.buf);
var G__38423 = (new Float32Array(3));
(G__38423[(0)] = (G__38422[(1)]));

(G__38423[(1)] = (G__38422[(0)]));

(G__38423[(2)] = (G__38422[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38423,null,cljs.core.meta.call(null,G__38424)));
});
thi.ng.geom.core.vector.yxz = (function thi$ng$geom$core$vector$yxz(G__38427){
if((G__38427 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38427","G__38427",220188462,null))))].join('')));
}

var G__38425 = (((G__38427 instanceof thi.ng.geom.core.vector.Vec2))?G__38427.buf:G__38427.buf);
var G__38426 = (new Float32Array(3));
(G__38426[(0)] = (G__38425[(1)]));

(G__38426[(1)] = (G__38425[(0)]));

(G__38426[(2)] = (G__38425[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38426,null,cljs.core.meta.call(null,G__38427)));
});
thi.ng.geom.core.vector.yy = (function thi$ng$geom$core$vector$yy(G__38430){
var G__38428 = (((G__38430 instanceof thi.ng.geom.core.vector.Vec2))?G__38430.buf:G__38430.buf);
var G__38429 = (new Float32Array(2));
(G__38429[(0)] = (G__38428[(1)]));

(G__38429[(1)] = (G__38428[(1)]));

return (new thi.ng.geom.core.vector.Vec2(G__38429,null,cljs.core.meta.call(null,G__38430)));
});
thi.ng.geom.core.vector.yyx = (function thi$ng$geom$core$vector$yyx(G__38433){
var G__38431 = (((G__38433 instanceof thi.ng.geom.core.vector.Vec2))?G__38433.buf:G__38433.buf);
var G__38432 = (new Float32Array(3));
(G__38432[(0)] = (G__38431[(1)]));

(G__38432[(1)] = (G__38431[(1)]));

(G__38432[(2)] = (G__38431[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38432,null,cljs.core.meta.call(null,G__38433)));
});
thi.ng.geom.core.vector.yyy = (function thi$ng$geom$core$vector$yyy(G__38436){
var G__38434 = (((G__38436 instanceof thi.ng.geom.core.vector.Vec2))?G__38436.buf:G__38436.buf);
var G__38435 = (new Float32Array(3));
(G__38435[(0)] = (G__38434[(1)]));

(G__38435[(1)] = (G__38434[(1)]));

(G__38435[(2)] = (G__38434[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38435,null,cljs.core.meta.call(null,G__38436)));
});
thi.ng.geom.core.vector.yyz = (function thi$ng$geom$core$vector$yyz(G__38439){
if((G__38439 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38439","G__38439",1984007843,null))))].join('')));
}

var G__38437 = (((G__38439 instanceof thi.ng.geom.core.vector.Vec2))?G__38439.buf:G__38439.buf);
var G__38438 = (new Float32Array(3));
(G__38438[(0)] = (G__38437[(1)]));

(G__38438[(1)] = (G__38437[(1)]));

(G__38438[(2)] = (G__38437[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38438,null,cljs.core.meta.call(null,G__38439)));
});
thi.ng.geom.core.vector.yz = (function thi$ng$geom$core$vector$yz(G__38442){
if((G__38442 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38442","G__38442",-434161602,null))))].join('')));
}

var G__38440 = (((G__38442 instanceof thi.ng.geom.core.vector.Vec2))?G__38442.buf:G__38442.buf);
var G__38441 = (new Float32Array(2));
(G__38441[(0)] = (G__38440[(1)]));

(G__38441[(1)] = (G__38440[(2)]));

return (new thi.ng.geom.core.vector.Vec2(G__38441,null,cljs.core.meta.call(null,G__38442)));
});
thi.ng.geom.core.vector.yzx = (function thi$ng$geom$core$vector$yzx(G__38445){
if((G__38445 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38445","G__38445",1451156582,null))))].join('')));
}

var G__38443 = (((G__38445 instanceof thi.ng.geom.core.vector.Vec2))?G__38445.buf:G__38445.buf);
var G__38444 = (new Float32Array(3));
(G__38444[(0)] = (G__38443[(1)]));

(G__38444[(1)] = (G__38443[(2)]));

(G__38444[(2)] = (G__38443[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38444,null,cljs.core.meta.call(null,G__38445)));
});
thi.ng.geom.core.vector.yzy = (function thi$ng$geom$core$vector$yzy(G__38448){
if((G__38448 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38448","G__38448",-357423414,null))))].join('')));
}

var G__38446 = (((G__38448 instanceof thi.ng.geom.core.vector.Vec2))?G__38448.buf:G__38448.buf);
var G__38447 = (new Float32Array(3));
(G__38447[(0)] = (G__38446[(1)]));

(G__38447[(1)] = (G__38446[(2)]));

(G__38447[(2)] = (G__38446[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38447,null,cljs.core.meta.call(null,G__38448)));
});
thi.ng.geom.core.vector.yzz = (function thi$ng$geom$core$vector$yzz(G__38451){
if((G__38451 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38451","G__38451",936211010,null))))].join('')));
}

var G__38449 = (((G__38451 instanceof thi.ng.geom.core.vector.Vec2))?G__38451.buf:G__38451.buf);
var G__38450 = (new Float32Array(3));
(G__38450[(0)] = (G__38449[(1)]));

(G__38450[(1)] = (G__38449[(2)]));

(G__38450[(2)] = (G__38449[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38450,null,cljs.core.meta.call(null,G__38451)));
});
thi.ng.geom.core.vector.z = (function thi$ng$geom$core$vector$z(G__38454){
if((G__38454 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38454","G__38454",-1178180486,null))))].join('')));
}

var G__38452 = (((G__38454 instanceof thi.ng.geom.core.vector.Vec2))?G__38454.buf:G__38454.buf);
return (G__38452[(2)]);
});
thi.ng.geom.core.vector.zx = (function thi$ng$geom$core$vector$zx(G__38457){
if((G__38457 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38457","G__38457",799153839,null))))].join('')));
}

var G__38455 = (((G__38457 instanceof thi.ng.geom.core.vector.Vec2))?G__38457.buf:G__38457.buf);
var G__38456 = (new Float32Array(2));
(G__38456[(0)] = (G__38455[(2)]));

(G__38456[(1)] = (G__38455[(0)]));

return (new thi.ng.geom.core.vector.Vec2(G__38456,null,cljs.core.meta.call(null,G__38457)));
});
thi.ng.geom.core.vector.zxx = (function thi$ng$geom$core$vector$zxx(G__38460){
if((G__38460 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38460","G__38460",-1072677890,null))))].join('')));
}

var G__38458 = (((G__38460 instanceof thi.ng.geom.core.vector.Vec2))?G__38460.buf:G__38460.buf);
var G__38459 = (new Float32Array(3));
(G__38459[(0)] = (G__38458[(2)]));

(G__38459[(1)] = (G__38458[(0)]));

(G__38459[(2)] = (G__38458[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38459,null,cljs.core.meta.call(null,G__38460)));
});
thi.ng.geom.core.vector.zxy = (function thi$ng$geom$core$vector$zxy(G__38463){
if((G__38463 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38463","G__38463",624484496,null))))].join('')));
}

var G__38461 = (((G__38463 instanceof thi.ng.geom.core.vector.Vec2))?G__38463.buf:G__38463.buf);
var G__38462 = (new Float32Array(3));
(G__38462[(0)] = (G__38461[(2)]));

(G__38462[(1)] = (G__38461[(0)]));

(G__38462[(2)] = (G__38461[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38462,null,cljs.core.meta.call(null,G__38463)));
});
thi.ng.geom.core.vector.zxz = (function thi$ng$geom$core$vector$zxz(G__38466){
if((G__38466 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38466","G__38466",-613004775,null))))].join('')));
}

var G__38464 = (((G__38466 instanceof thi.ng.geom.core.vector.Vec2))?G__38466.buf:G__38466.buf);
var G__38465 = (new Float32Array(3));
(G__38465[(0)] = (G__38464[(2)]));

(G__38465[(1)] = (G__38464[(0)]));

(G__38465[(2)] = (G__38464[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38465,null,cljs.core.meta.call(null,G__38466)));
});
thi.ng.geom.core.vector.zy = (function thi$ng$geom$core$vector$zy(G__38469){
if((G__38469 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38469","G__38469",-890606430,null))))].join('')));
}

var G__38467 = (((G__38469 instanceof thi.ng.geom.core.vector.Vec2))?G__38469.buf:G__38469.buf);
var G__38468 = (new Float32Array(2));
(G__38468[(0)] = (G__38467[(2)]));

(G__38468[(1)] = (G__38467[(1)]));

return (new thi.ng.geom.core.vector.Vec2(G__38468,null,cljs.core.meta.call(null,G__38469)));
});
thi.ng.geom.core.vector.zyx = (function thi$ng$geom$core$vector$zyx(G__38472){
if((G__38472 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38472","G__38472",228546190,null))))].join('')));
}

var G__38470 = (((G__38472 instanceof thi.ng.geom.core.vector.Vec2))?G__38472.buf:G__38472.buf);
var G__38471 = (new Float32Array(3));
(G__38471[(0)] = (G__38470[(2)]));

(G__38471[(1)] = (G__38470[(1)]));

(G__38471[(2)] = (G__38470[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38471,null,cljs.core.meta.call(null,G__38472)));
});
thi.ng.geom.core.vector.zyy = (function thi$ng$geom$core$vector$zyy(G__38475){
if((G__38475 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38475","G__38475",1459623383,null))))].join('')));
}

var G__38473 = (((G__38475 instanceof thi.ng.geom.core.vector.Vec2))?G__38475.buf:G__38475.buf);
var G__38474 = (new Float32Array(3));
(G__38474[(0)] = (G__38473[(2)]));

(G__38474[(1)] = (G__38473[(1)]));

(G__38474[(2)] = (G__38473[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38474,null,cljs.core.meta.call(null,G__38475)));
});
thi.ng.geom.core.vector.zyz = (function thi$ng$geom$core$vector$zyz(G__38478){
if((G__38478 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38478","G__38478",-39555890,null))))].join('')));
}

var G__38476 = (((G__38478 instanceof thi.ng.geom.core.vector.Vec2))?G__38478.buf:G__38478.buf);
var G__38477 = (new Float32Array(3));
(G__38477[(0)] = (G__38476[(2)]));

(G__38477[(1)] = (G__38476[(1)]));

(G__38477[(2)] = (G__38476[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38477,null,cljs.core.meta.call(null,G__38478)));
});
thi.ng.geom.core.vector.zz = (function thi$ng$geom$core$vector$zz(G__38481){
if((G__38481 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38481","G__38481",-1414294648,null))))].join('')));
}

var G__38479 = (((G__38481 instanceof thi.ng.geom.core.vector.Vec2))?G__38481.buf:G__38481.buf);
var G__38480 = (new Float32Array(2));
(G__38480[(0)] = (G__38479[(2)]));

(G__38480[(1)] = (G__38479[(2)]));

return (new thi.ng.geom.core.vector.Vec2(G__38480,null,cljs.core.meta.call(null,G__38481)));
});
thi.ng.geom.core.vector.zzx = (function thi$ng$geom$core$vector$zzx(G__38484){
if((G__38484 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38484","G__38484",1599814559,null))))].join('')));
}

var G__38482 = (((G__38484 instanceof thi.ng.geom.core.vector.Vec2))?G__38484.buf:G__38484.buf);
var G__38483 = (new Float32Array(3));
(G__38483[(0)] = (G__38482[(2)]));

(G__38483[(1)] = (G__38482[(2)]));

(G__38483[(2)] = (G__38482[(0)]));

return (new thi.ng.geom.core.vector.Vec3(G__38483,null,cljs.core.meta.call(null,G__38484)));
});
thi.ng.geom.core.vector.zzy = (function thi$ng$geom$core$vector$zzy(G__38487){
if((G__38487 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38487","G__38487",1435361835,null))))].join('')));
}

var G__38485 = (((G__38487 instanceof thi.ng.geom.core.vector.Vec2))?G__38487.buf:G__38487.buf);
var G__38486 = (new Float32Array(3));
(G__38486[(0)] = (G__38485[(2)]));

(G__38486[(1)] = (G__38485[(2)]));

(G__38486[(2)] = (G__38485[(1)]));

return (new thi.ng.geom.core.vector.Vec3(G__38486,null,cljs.core.meta.call(null,G__38487)));
});
thi.ng.geom.core.vector.zzz = (function thi$ng$geom$core$vector$zzz(G__38490){
if((G__38490 instanceof thi.ng.geom.core.vector.Vec3)){
} else {
throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.list(new cljs.core.Symbol("clojure.core","instance?","clojure.core/instance?",2143709132,null),new cljs.core.Symbol(null,"Vec3","Vec3",429395803,null),new cljs.core.Symbol(null,"G__38490","G__38490",-1019059166,null))))].join('')));
}

var G__38488 = (((G__38490 instanceof thi.ng.geom.core.vector.Vec2))?G__38490.buf:G__38490.buf);
var G__38489 = (new Float32Array(3));
(G__38489[(0)] = (G__38488[(2)]));

(G__38489[(1)] = (G__38488[(2)]));

(G__38489[(2)] = (G__38488[(2)]));

return (new thi.ng.geom.core.vector.Vec3(G__38489,null,cljs.core.meta.call(null,G__38490)));
});
thi.ng.geom.core.vector.swizzle2_fns = new cljs.core.PersistentArrayMap(null, 6, [new cljs.core.Keyword(null,"x","x",2099068185),thi.ng.geom.core.vector.x,new cljs.core.Keyword(null,"xx","xx",-1542203733),thi.ng.geom.core.vector.xx,new cljs.core.Keyword(null,"xy","xy",-696978232),thi.ng.geom.core.vector.xy,new cljs.core.Keyword(null,"y","y",-1757859776),thi.ng.geom.core.vector.y,new cljs.core.Keyword(null,"yx","yx",1696579752),thi.ng.geom.core.vector.yx,new cljs.core.Keyword(null,"yy","yy",-1432012814),thi.ng.geom.core.vector.yy], null);
thi.ng.geom.core.vector.swizzle3_fns = cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"y","y",-1757859776),new cljs.core.Keyword(null,"xzx","xzx",-1000197983),new cljs.core.Keyword(null,"xyz","xyz",-1605570015),new cljs.core.Keyword(null,"zzy","zzy",-874287326),new cljs.core.Keyword(null,"yx","yx",1696579752),new cljs.core.Keyword(null,"xy","xy",-696978232),new cljs.core.Keyword(null,"yyz","yyz",1133968296),new cljs.core.Keyword(null,"zxy","zxy",-1258840183),new cljs.core.Keyword(null,"xzy","xzy",1043177385),new cljs.core.Keyword(null,"zxz","zxz",1026042602),new cljs.core.Keyword(null,"zx","zx",-933582998),new cljs.core.Keyword(null,"xx","xx",-1542203733),new cljs.core.Keyword(null,"xxx","xxx",-1019301908),new cljs.core.Keyword(null,"zy","zy",-1975963090),new cljs.core.Keyword(null,"zzx","zzx",20750383),new cljs.core.Keyword(null,"zyx","zyx",1752527951),new cljs.core.Keyword(null,"yzx","yzx",-1496223025),new cljs.core.Keyword(null,"z","z",-789527183),new cljs.core.Keyword(null,"yyx","yyx",-1318218191),new cljs.core.Keyword(null,"xz","xz",426487154),new cljs.core.Keyword(null,"zyz","zyz",-1838068142),new cljs.core.Keyword(null,"yy","yy",-1432012814),new cljs.core.Keyword(null,"xxz","xxz",129827699),new cljs.core.Keyword(null,"yzy","yzy",-179510251),new cljs.core.Keyword(null,"yz","yz",679015029),new cljs.core.Keyword(null,"yxx","yxx",-332290091),new cljs.core.Keyword(null,"xyy","xyy",996073014),new cljs.core.Keyword(null,"xxy","xxy",-650102026),new cljs.core.Keyword(null,"zz","zz",122901783),new cljs.core.Keyword(null,"zzz","zzz",-77420552),new cljs.core.Keyword(null,"x","x",2099068185),new cljs.core.Keyword(null,"xzz","xzz",-643126693),new cljs.core.Keyword(null,"yxz","yxz",1786796508),new cljs.core.Keyword(null,"zxx","zxx",-61980804),new cljs.core.Keyword(null,"yzz","yzz",-1034441732),new cljs.core.Keyword(null,"xyx","xyx",1899467293),new cljs.core.Keyword(null,"yxy","yxy",1369901661),new cljs.core.Keyword(null,"yyy","yyy",780595422),new cljs.core.Keyword(null,"zyy","zyy",1946268991)],[thi.ng.geom.core.vector.y,thi.ng.geom.core.vector.xzx,thi.ng.geom.core.vector.xyz,thi.ng.geom.core.vector.zzy,thi.ng.geom.core.vector.yx,thi.ng.geom.core.vector.xy,thi.ng.geom.core.vector.yyz,thi.ng.geom.core.vector.zxy,thi.ng.geom.core.vector.xzy,thi.ng.geom.core.vector.zxz,thi.ng.geom.core.vector.zx,thi.ng.geom.core.vector.xx,thi.ng.geom.core.vector.xxx,thi.ng.geom.core.vector.zy,thi.ng.geom.core.vector.zzx,thi.ng.geom.core.vector.zyx,thi.ng.geom.core.vector.yzx,thi.ng.geom.core.vector.z,thi.ng.geom.core.vector.yyx,thi.ng.geom.core.vector.xz,thi.ng.geom.core.vector.zyz,thi.ng.geom.core.vector.yy,thi.ng.geom.core.vector.xxz,thi.ng.geom.core.vector.yzy,thi.ng.geom.core.vector.yz,thi.ng.geom.core.vector.yxx,thi.ng.geom.core.vector.xyy,thi.ng.geom.core.vector.xxy,thi.ng.geom.core.vector.zz,thi.ng.geom.core.vector.zzz,thi.ng.geom.core.vector.x,thi.ng.geom.core.vector.xzz,thi.ng.geom.core.vector.yxz,thi.ng.geom.core.vector.zxx,thi.ng.geom.core.vector.yzz,thi.ng.geom.core.vector.xyx,thi.ng.geom.core.vector.yxy,thi.ng.geom.core.vector.yyy,thi.ng.geom.core.vector.zyy]);
thi.ng.geom.core.vector.swizzle_assoc_STAR_ = (function thi$ng$geom$core$vector$swizzle_assoc_STAR_(src,dest,keymap,k,v){
var n = cljs.core.name.call(null,k);
var c = cljs.core.count.call(null,n);
var temp__4423__auto__ = (function (){var and__16057__auto__ = ((1) === c);
if(and__16057__auto__){
return keymap.call(null,cljs.core.first.call(null,n));
} else {
return and__16057__auto__;
}
})();
if(cljs.core.truth_(temp__4423__auto__)){
var idx = temp__4423__auto__;
(dest[(idx | (0))] = v);

return dest;
} else {
if(((c <= cljs.core.count.call(null,keymap))) && (((c === cljs.core.count.call(null,v))) && ((cljs.core.count.call(null,v) === cljs.core.count.call(null,cljs.core.into.call(null,cljs.core.PersistentHashSet.EMPTY,n)))))){
var i = (0);
var n__$1 = n;
while(true){
if(cljs.core.truth_(n__$1)){
var temp__4423__auto____$1 = keymap.call(null,cljs.core.first.call(null,n__$1));
if(cljs.core.truth_(temp__4423__auto____$1)){
var idx = temp__4423__auto____$1;
(dest[(idx | (0))] = v.call(null,i));

var G__38491 = (i + (1));
var G__38492 = cljs.core.next.call(null,n__$1);
i = G__38491;
n__$1 = G__38492;
continue;
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
} else {
return dest;
}
break;
}
} else {
return thi.ng.common.error.key_error_BANG_.call(null,k);
}
}
});
thi.ng.geom.core.vector.vec2_reduce_STAR_ = (function thi$ng$geom$core$vector$vec2_reduce_STAR_(op,acc,xs){
return cljs.core.transduce.call(null,cljs.core.map.call(null,(function (x){
return x.buf;
})),(function() {
var G__38493 = null;
var G__38493__1 = (function (a){
return a;
});
var G__38493__2 = (function (a,b){
(a[(0)] = op.call(null,(a[(0)]),(b[(0)])));

(a[(1)] = op.call(null,(a[(1)]),(b[(1)])));

return a;
});
G__38493 = function(a,b){
switch(arguments.length){
case 1:
return G__38493__1.call(this,a);
case 2:
return G__38493__2.call(this,a,b);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
G__38493.cljs$core$IFn$_invoke$arity$1 = G__38493__1;
G__38493.cljs$core$IFn$_invoke$arity$2 = G__38493__2;
return G__38493;
})()
,acc,xs);
});
thi.ng.geom.core.vector.vec3_reduce_STAR_ = (function thi$ng$geom$core$vector$vec3_reduce_STAR_(op,acc,xs){
return cljs.core.transduce.call(null,cljs.core.map.call(null,(function (x){
return x.buf;
})),(function() {
var G__38494 = null;
var G__38494__1 = (function (a){
return a;
});
var G__38494__2 = (function (a,b){
(a[(0)] = op.call(null,(a[(0)]),(b[(0)])));

(a[(1)] = op.call(null,(a[(1)]),(b[(1)])));

(a[(2)] = op.call(null,(a[(2)]),(b[(2)])));

return a;
});
G__38494 = function(a,b){
switch(arguments.length){
case 1:
return G__38494__1.call(this,a);
case 2:
return G__38494__2.call(this,a,b);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
G__38494.cljs$core$IFn$_invoke$arity$1 = G__38494__1;
G__38494.cljs$core$IFn$_invoke$arity$2 = G__38494__2;
return G__38494;
})()
,acc,xs);
});
thi.ng.geom.core.vector.V2 = (new thi.ng.geom.core.vector.Vec2((new Float32Array((2))),null,null));
thi.ng.geom.core.vector.V3 = (new thi.ng.geom.core.vector.Vec3((new Float32Array((3))),null,null));
thi.ng.geom.core.vector.vec2 = (function thi$ng$geom$core$vector$vec2(){
var G__38496 = arguments.length;
switch (G__38496) {
case 0:
return thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.vector.V2;
});

thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$1 = (function (v){
if((v instanceof thi.ng.geom.core.vector.Vec2)){
return v;
} else {
if(typeof v === 'number'){
return thi.ng.geom.core.vector.vec2.call(null,v,v);
} else {
if(cljs.core.sequential_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec2.call(null,cljs.core.nth.call(null,v,(0),0.0),cljs.core.nth.call(null,v,(1),0.0));
} else {
if(cljs.core.map_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec2.call(null,new cljs.core.Keyword(null,"x","x",2099068185).cljs$core$IFn$_invoke$arity$2(v,(0)),new cljs.core.Keyword(null,"y","y",-1757859776).cljs$core$IFn$_invoke$arity$2(v,(0)));
} else {
return thi.ng.common.error.type_error_BANG_.call(null,"Vec2",v);

}
}
}
}
});

thi.ng.geom.core.vector.vec2.cljs$core$IFn$_invoke$arity$2 = (function (x,y){
var b = (new Float32Array((2)));
(b[(0)] = x);

(b[(1)] = y);

return (new thi.ng.geom.core.vector.Vec2(b,null,null));
});

thi.ng.geom.core.vector.vec2.cljs$lang$maxFixedArity = 2;
thi.ng.geom.core.vector.vec3 = (function thi$ng$geom$core$vector$vec3(){
var G__38499 = arguments.length;
switch (G__38499) {
case 0:
return thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 2:
return thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.vector.V3;
});

thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$1 = (function (v){
if((v instanceof thi.ng.geom.core.vector.Vec3)){
return v;
} else {
if(typeof v === 'number'){
return thi.ng.geom.core.vector.vec3.call(null,v,v,v);
} else {
if(cljs.core.sequential_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3.call(null,cljs.core.nth.call(null,v,(0),0.0),cljs.core.nth.call(null,v,(1),0.0),cljs.core.nth.call(null,v,(2),0.0));
} else {
if(cljs.core.map_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3.call(null,new cljs.core.Keyword(null,"x","x",2099068185).cljs$core$IFn$_invoke$arity$2(v,(0)),new cljs.core.Keyword(null,"y","y",-1757859776).cljs$core$IFn$_invoke$arity$2(v,(0)),new cljs.core.Keyword(null,"z","z",-789527183).cljs$core$IFn$_invoke$arity$2(v,(0)));
} else {
return thi.ng.common.error.type_error_BANG_.call(null,"Vec3",v);

}
}
}
}
});

thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$2 = (function (v,z){
if(cljs.core.sequential_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3.call(null,cljs.core.nth.call(null,v,(0),0.0),cljs.core.nth.call(null,v,(1),0.0),z);
} else {
if(cljs.core.map_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3.call(null,new cljs.core.Keyword(null,"x","x",2099068185).cljs$core$IFn$_invoke$arity$2(v,(0)),new cljs.core.Keyword(null,"y","y",-1757859776).cljs$core$IFn$_invoke$arity$2(v,(0)),z);
} else {
if(typeof v === 'number'){
return thi.ng.geom.core.vector.vec3.call(null,v,z,(0));
} else {
return thi.ng.common.error.type_error_BANG_.call(null,"Vec3",v);

}
}
}
});

thi.ng.geom.core.vector.vec3.cljs$core$IFn$_invoke$arity$3 = (function (x,y,z){
var b = (new Float32Array((3)));
(b[(0)] = x);

(b[(1)] = y);

(b[(2)] = z);

return (new thi.ng.geom.core.vector.Vec3(b,null,null));
});

thi.ng.geom.core.vector.vec3.cljs$lang$maxFixedArity = 3;
thi.ng.geom.core.vector.vec2_with_meta = (function thi$ng$geom$core$vector$vec2_with_meta(){
var G__38502 = arguments.length;
switch (G__38502) {
case 2:
return thi.ng.geom.core.vector.vec2_with_meta.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.geom.core.vector.vec2_with_meta.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.vec2_with_meta.cljs$core$IFn$_invoke$arity$2 = (function (v,meta){
if((v instanceof thi.ng.geom.core.vector.Vec2)){
return cljs.core.with_meta.call(null,v,meta);
} else {
if(typeof v === 'number'){
return thi.ng.geom.core.vector.vec2_with_meta.call(null,v,v,meta);
} else {
if(cljs.core.sequential_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec2_with_meta.call(null,cljs.core.nth.call(null,v,(0),0.0),cljs.core.nth.call(null,v,(1),0.0),meta);
} else {
if(cljs.core.map_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec2_with_meta.call(null,new cljs.core.Keyword(null,"x","x",2099068185).cljs$core$IFn$_invoke$arity$2(v,0.0),new cljs.core.Keyword(null,"y","y",-1757859776).cljs$core$IFn$_invoke$arity$2(v,0.0),meta);
} else {
return thi.ng.common.error.type_error_BANG_.call(null,"Vec2",v);

}
}
}
}
});

thi.ng.geom.core.vector.vec2_with_meta.cljs$core$IFn$_invoke$arity$3 = (function (x,y,meta){
var b = (new Float32Array((2)));
(b[(0)] = x);

(b[(1)] = y);

return (new thi.ng.geom.core.vector.Vec2(b,null,meta));
});

thi.ng.geom.core.vector.vec2_with_meta.cljs$lang$maxFixedArity = 3;
thi.ng.geom.core.vector.vec3_with_meta = (function thi$ng$geom$core$vector$vec3_with_meta(){
var G__38505 = arguments.length;
switch (G__38505) {
case 2:
return thi.ng.geom.core.vector.vec3_with_meta.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 4:
return thi.ng.geom.core.vector.vec3_with_meta.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.vec3_with_meta.cljs$core$IFn$_invoke$arity$2 = (function (v,meta){
if((v instanceof thi.ng.geom.core.vector.Vec3)){
return cljs.core.with_meta.call(null,v,meta);
} else {
if(typeof v === 'number'){
return thi.ng.geom.core.vector.vec3_with_meta.call(null,v,v,v,meta);
} else {
if(cljs.core.sequential_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3_with_meta.call(null,cljs.core.nth.call(null,v,(0),0.0),cljs.core.nth.call(null,v,(1),0.0),cljs.core.nth.call(null,v,(2),0.0),meta);
} else {
if(cljs.core.map_QMARK_.call(null,v)){
return thi.ng.geom.core.vector.vec3_with_meta.call(null,new cljs.core.Keyword(null,"x","x",2099068185).cljs$core$IFn$_invoke$arity$2(v,0.0),new cljs.core.Keyword(null,"y","y",-1757859776).cljs$core$IFn$_invoke$arity$2(v,0.0),new cljs.core.Keyword(null,"z","z",-789527183).cljs$core$IFn$_invoke$arity$2(v,0.0),meta);
} else {
return thi.ng.common.error.type_error_BANG_.call(null,"Vec3",v);

}
}
}
}
});

thi.ng.geom.core.vector.vec3_with_meta.cljs$core$IFn$_invoke$arity$4 = (function (x,y,z,meta){
var b = (new Float32Array((3)));
(b[(0)] = x);

(b[(1)] = y);

(b[(2)] = z);

return (new thi.ng.geom.core.vector.Vec3(b,null,meta));
});

thi.ng.geom.core.vector.vec3_with_meta.cljs$lang$maxFixedArity = 4;
thi.ng.geom.core.vector.vec2_QMARK_ = (function thi$ng$geom$core$vector$vec2_QMARK_(x){
return (x instanceof thi.ng.geom.core.vector.Vec2);
});
thi.ng.geom.core.vector.vec3_QMARK_ = (function thi$ng$geom$core$vector$vec3_QMARK_(x){
return (x instanceof thi.ng.geom.core.vector.Vec3);
});
thi.ng.geom.core.vector.V2X = thi.ng.geom.core.vector.vec2.call(null,(1),(0));
thi.ng.geom.core.vector.V2Y = thi.ng.geom.core.vector.vec2.call(null,(0),(1));
thi.ng.geom.core.vector.V3X = thi.ng.geom.core.vector.vec3.call(null,(1),(0),(0));
thi.ng.geom.core.vector.V3Y = thi.ng.geom.core.vector.vec3.call(null,(0),(1),(0));
thi.ng.geom.core.vector.V3Z = thi.ng.geom.core.vector.vec3.call(null,(0),(0),(1));
thi.ng.geom.core.vector.V2INF_ = thi.ng.geom.core.vector.vec2.call(null,thi.ng.math.core.INF_);
thi.ng.geom.core.vector.V2INF_PLUS_ = thi.ng.geom.core.vector.vec2.call(null,thi.ng.math.core.INF_PLUS_);
thi.ng.geom.core.vector.V3INF_ = thi.ng.geom.core.vector.vec3.call(null,thi.ng.math.core.INF_);
thi.ng.geom.core.vector.V3INF_PLUS_ = thi.ng.geom.core.vector.vec3.call(null,thi.ng.math.core.INF_PLUS_);
thi.ng.geom.core.vector.randvec2 = (function thi$ng$geom$core$vector$randvec2(){
var G__38508 = arguments.length;
switch (G__38508) {
case 0:
return thi.ng.geom.core.vector.randvec2.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.vector.randvec2.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.randvec2.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.normalize.call(null,thi.ng.geom.core.vector.vec2.call(null,thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null)));
});

thi.ng.geom.core.vector.randvec2.cljs$core$IFn$_invoke$arity$1 = (function (n){
return thi.ng.geom.core.normalize.call(null,thi.ng.geom.core.vector.vec2.call(null,thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null)),n);
});

thi.ng.geom.core.vector.randvec2.cljs$lang$maxFixedArity = 1;
thi.ng.geom.core.vector.randvec3 = (function thi$ng$geom$core$vector$randvec3(){
var G__38511 = arguments.length;
switch (G__38511) {
case 0:
return thi.ng.geom.core.vector.randvec3.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.vector.randvec3.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.vector.randvec3.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.normalize.call(null,thi.ng.geom.core.vector.vec3.call(null,thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null)));
});

thi.ng.geom.core.vector.randvec3.cljs$core$IFn$_invoke$arity$1 = (function (n){
return thi.ng.geom.core.normalize.call(null,thi.ng.geom.core.vector.vec3.call(null,thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null),thi.ng.math.core.randnorm.call(null)),n);
});

thi.ng.geom.core.vector.randvec3.cljs$lang$maxFixedArity = 1;

//# sourceMappingURL=vector.js.map?rel=1439206055396