// Compiled by ClojureScript 0.0-3297 {}
goog.provide('thi.ng.geom.core.matrix');
goog.require('cljs.core');
goog.require('thi.ng.geom.core.utils');
goog.require('thi.ng.geom.core');
goog.require('thi.ng.geom.core.vector');
goog.require('thi.ng.math.core');
goog.require('thi.ng.common.error');

/**
* @constructor
*/
thi.ng.geom.core.matrix.Matrix32 = (function (m00,m01,m02,m10,m11,m12,_hasheq,_meta){
this.m00 = m00;
this.m01 = m01;
this.m02 = m02;
this.m10 = m10;
this.m11 = m11;
this.m12 = m12;
this._hasheq = _hasheq;
this._meta = _meta;
this.cljs$lang$protocol_mask$partition0$ = 31850714;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PVectorTransform$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PVectorTransform$transform_vector$arity$2 = (function (_,p__31014){
var self__ = this;
var vec__31015 = p__31014;
var x = cljs.core.nth.call(null,vec__31015,(0),null);
var y = cljs.core.nth.call(null,vec__31015,(1),null);
var v = vec__31015;
var ___$1 = this;
var b = (new Float32Array((2)));
(b[(0)] = (((x * self__.m00) + (y * self__.m01)) + self__.m02));

(b[(1)] = (((x * self__.m10) + (y * self__.m11)) + self__.m12));

return (new thi.ng.geom.core.vector.Vec2(b,null,cljs.core.meta.call(null,v)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTransform$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTransform$transform$arity$2 = (function (_,matrix){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,matrix);
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PRotate$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PRotate$rotate$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(c,(- s),0.0,s,c,0.0,null,self__._meta)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTranspose$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTranspose$transpose$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix32(self__.m00,self__.m10,self__.m01,self__.m11,self__.m02,self__.m12,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix32.prototype.toString = (function (){
var self__ = this;
var _ = this;
return [cljs.core.str("["),cljs.core.str(self__.m00),cljs.core.str(" "),cljs.core.str(self__.m01),cljs.core.str(" "),cljs.core.str(self__.m02),cljs.core.str(" "),cljs.core.str(self__.m10),cljs.core.str(" "),cljs.core.str(self__.m11),cljs.core.str(" "),cljs.core.str(self__.m12),cljs.core.str("]")].join('');
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IIndexed$_nth$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
var G__31016 = (k | (0));
switch (G__31016) {
case (0):
return self__.m00;

break;
case (1):
return self__.m01;

break;
case (2):
return self__.m02;

break;
case (3):
return self__.m10;

break;
case (4):
return self__.m11;

break;
case (5):
return self__.m12;

break;
default:
return thi.ng.common.error.illegal_arg_BANG_.call(null,k);

}
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IIndexed$_nth$arity$3 = (function (_,k,nf){
var self__ = this;
var ___$1 = this;
var G__31017 = (k | (0));
switch (G__31017) {
case (0):
return self__.m00;

break;
case (1):
return self__.m01;

break;
case (2):
return self__.m02;

break;
case (3):
return self__.m10;

break;
case (4):
return self__.m11;

break;
case (5):
return self__.m12;

break;
default:
return nf;

}
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__._meta;
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$INext$_next$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [self__.m01,self__.m02,self__.m10,self__.m11,self__.m12], null));
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$ICounted$_count$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (6);
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PDeterminant$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PDeterminant$determinant$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ((self__.m00 * self__.m11) - (self__.m01 * self__.m10));
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IHash$_hash$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var or__16069__auto__ = self__._hasheq;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return self__._hasheq = cljs.core.hash_ordered_coll.call(null,___$1);
}
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IEquiv$_equiv$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
return (cljs.core.sequential_QMARK_.call(null,o)) && (((6) === cljs.core.count.call(null,o))) && (cljs.core.every_QMARK_.call(null,((function (___$1){
return (function (p1__31013_SHARP_){
return cljs.core._EQ_.call(null,p1__31013_SHARP_.call(null,(0)),p1__31013_SHARP_.call(null,(1)));
});})(___$1))
,cljs.core.map.call(null,cljs.core.vector,___$1,o)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PInvert$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PInvert$invert$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var d = thi.ng.geom.core.determinant.call(null,___$1);
if((d === (0))){
return null;
} else {
return (new thi.ng.geom.core.matrix.Matrix32((self__.m11 / d),(- (self__.m01 / d)),(((self__.m01 * self__.m12) - (self__.m11 * self__.m02)) / d),(- (self__.m10 / d)),(self__.m00 / d),(((self__.m10 * self__.m02) - (self__.m00 * self__.m12)) / d),null,self__._meta));
}
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PMathOps$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix32((self__.m00 + m__$1.m00),(self__.m01 + m__$1.m01),(self__.m02 + m__$1.m02),(self__.m10 + m__$1.m10),(self__.m11 + m__$1.m11),(self__.m12 + m__$1.m12),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PMathOps$_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix32((self__.m00 - m__$1.m00),(self__.m01 - m__$1.m01),(self__.m02 - m__$1.m02),(self__.m10 - m__$1.m10),(self__.m11 - m__$1.m11),(self__.m12 - m__$1.m12),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix32(((self__.m00 * m__$1.m00) + (self__.m01 * m__$1.m10)),((self__.m00 * m__$1.m01) + (self__.m01 * m__$1.m11)),(((self__.m00 * m__$1.m02) + (self__.m01 * m__$1.m12)) + self__.m02),((self__.m10 * m__$1.m00) + (self__.m11 * m__$1.m10)),((self__.m10 * m__$1.m01) + (self__.m11 * m__$1.m11)),(((self__.m10 * m__$1.m02) + (self__.m11 * m__$1.m12)) + self__.m12),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$ISeq$_rest$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 5, 5, cljs.core.PersistentVector.EMPTY_NODE, [self__.m01,self__.m02,self__.m10,self__.m11,self__.m12], null));
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$ISeq$_first$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.m00;
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTranslate$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTranslate$translate$arity$2 = (function (_,t){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,((typeof t === 'number')?t:t.call(null,(0))),0.0,1.0,((typeof t === 'number')?t:t.call(null,(1))),null,self__._meta)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PTranslate$translate$arity$3 = (function (_,tx,ty){
var self__ = this;
var ___$1 = this;
if(typeof tx === 'number'){
if(typeof ty === 'number'){
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,tx,0.0,1.0,ty,null,self__._meta)));
} else {
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,(tx * ty.call(null,(0))),0.0,1.0,(tx * ty.call(null,(1))),null,self__._meta)));
}
} else {
if(typeof ty === 'number'){
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,(ty * tx.call(null,(0))),0.0,1.0,(ty * tx.call(null,(1))),null,self__._meta)));
} else {
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,(tx.call(null,(0)) * ty.call(null,(0))),0.0,1.0,(tx.call(null,(1)) * ty.call(null,(1))),null,self__._meta)));
}
}
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$ISeqable$_seq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PScale$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PScale$scale$arity$2 = (function (_,s){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(((typeof s === 'number')?s:s.call(null,(0))),0.0,0.0,0.0,((typeof s === 'number')?s:s.call(null,(1))),0.0,null,self__._meta)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PScale$scale$arity$3 = (function (_,sx,sy){
var self__ = this;
var ___$1 = this;
if(typeof sx === 'number'){
if(typeof sy === 'number'){
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(sx,0.0,0.0,0.0,sy,0.0,null,self__._meta)));
} else {
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32((sx * sy.call(null,(0))),0.0,0.0,0.0,(sx * sy.call(null,(1))),0.0,null,self__._meta)));
}
} else {
if(typeof sy === 'number'){
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32((sy * sx.call(null,(0))),0.0,0.0,0.0,(sy * sx.call(null,(1))),0.0,null,self__._meta)));
} else {
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32((sx.call(null,(0)) * sy.call(null,(0))),0.0,0.0,0.0,(sx.call(null,(1)) * sy.call(null,(1))),0.0,null,self__._meta)));
}
}
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix32(self__.m00,self__.m01,self__.m02,self__.m10,self__.m11,self__.m12,self__._hasheq,m));
});

thi.ng.geom.core.matrix.Matrix32.prototype.cljs$core$ICollection$_conj$arity$2 = (function (_,x){
var self__ = this;
var ___$1 = this;
return new cljs.core.PersistentVector(null, 7, 5, cljs.core.PersistentVector.EMPTY_NODE, [self__.m00,self__.m01,self__.m02,self__.m10,self__.m11,self__.m12,x], null);
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PShear$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PShear$shear$arity$2 = (function (_,s){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,((typeof s === 'number')?s:s.call(null,(0))),0.0,((typeof s === 'number')?s:s.call(null,(1))),1.0,0.0,null,self__._meta)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$geom$core$PShear$shear$arity$3 = (function (_,sx,sy){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,(new thi.ng.geom.core.matrix.Matrix32(1.0,sx,0.0,sy,1.0,0.0,null,self__._meta)));
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$math$core$PDeltaEquals$ = true;

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,___$1,m,thi.ng.math.core._STAR_eps_STAR_);
});

thi.ng.geom.core.matrix.Matrix32.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$3 = (function (_,m,eps){
var self__ = this;
var ___$1 = this;
var and__16057__auto__ = cljs.core.sequential_QMARK_.call(null,m);
if(and__16057__auto__){
var and__16057__auto____$1 = ((6) === cljs.core.count.call(null,m));
if(and__16057__auto____$1){
var and__16057__auto____$2 = thi.ng.math.core.delta_EQ_.call(null,self__.m00,cljs.core.first.call(null,m),eps);
if(cljs.core.truth_(and__16057__auto____$2)){
var and__16057__auto____$3 = thi.ng.math.core.delta_EQ_.call(null,self__.m01,cljs.core.nth.call(null,m,(1)),eps);
if(cljs.core.truth_(and__16057__auto____$3)){
var and__16057__auto____$4 = thi.ng.math.core.delta_EQ_.call(null,self__.m02,cljs.core.nth.call(null,m,(2)),eps);
if(cljs.core.truth_(and__16057__auto____$4)){
var and__16057__auto____$5 = thi.ng.math.core.delta_EQ_.call(null,self__.m10,cljs.core.nth.call(null,m,(3)),eps);
if(cljs.core.truth_(and__16057__auto____$5)){
var and__16057__auto____$6 = thi.ng.math.core.delta_EQ_.call(null,self__.m11,cljs.core.nth.call(null,m,(4)),eps);
if(cljs.core.truth_(and__16057__auto____$6)){
return thi.ng.math.core.delta_EQ_.call(null,self__.m12,cljs.core.nth.call(null,m,(5)),eps);
} else {
return and__16057__auto____$6;
}
} else {
return and__16057__auto____$5;
}
} else {
return and__16057__auto____$4;
}
} else {
return and__16057__auto____$3;
}
} else {
return and__16057__auto____$2;
}
} else {
return and__16057__auto____$1;
}
} else {
return and__16057__auto__;
}
});

thi.ng.geom.core.matrix.Matrix32.getBasis = (function (){
return new cljs.core.PersistentVector(null, 8, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"m00","m00",236909271,null),new cljs.core.Symbol(null,"m01","m01",-1573844190,null),new cljs.core.Symbol(null,"m02","m02",1667378534,null),new cljs.core.Symbol(null,"m10","m10",930266779,null),new cljs.core.Symbol(null,"m11","m11",-106606220,null),new cljs.core.Symbol(null,"m12","m12",405722345,null),new cljs.core.Symbol(null,"_hasheq","_hasheq",-1509801429,null),new cljs.core.Symbol(null,"_meta","_meta",-1716892533,null)], null);
});

thi.ng.geom.core.matrix.Matrix32.cljs$lang$type = true;

thi.ng.geom.core.matrix.Matrix32.cljs$lang$ctorStr = "thi.ng.geom.core.matrix/Matrix32";

thi.ng.geom.core.matrix.Matrix32.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"thi.ng.geom.core.matrix/Matrix32");
});

thi.ng.geom.core.matrix.__GT_Matrix32 = (function thi$ng$geom$core$matrix$__GT_Matrix32(m00,m01,m02,m10,m11,m12,_hasheq,_meta){
return (new thi.ng.geom.core.matrix.Matrix32(m00,m01,m02,m10,m11,m12,_hasheq,_meta));
});


/**
* @constructor
*/
thi.ng.geom.core.matrix.Matrix44 = (function (m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33,_hasheq,_meta){
this.m00 = m00;
this.m01 = m01;
this.m02 = m02;
this.m03 = m03;
this.m10 = m10;
this.m11 = m11;
this.m12 = m12;
this.m13 = m13;
this.m20 = m20;
this.m21 = m21;
this.m22 = m22;
this.m23 = m23;
this.m30 = m30;
this.m31 = m31;
this.m32 = m32;
this.m33 = m33;
this._hasheq = _hasheq;
this._meta = _meta;
this.cljs$lang$protocol_mask$partition0$ = 31850714;
this.cljs$lang$protocol_mask$partition1$ = 0;
})
thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PVectorTransform$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PVectorTransform$transform_vector$arity$2 = (function (_,p__31023){
var self__ = this;
var vec__31024 = p__31023;
var x = cljs.core.nth.call(null,vec__31024,(0),null);
var y = cljs.core.nth.call(null,vec__31024,(1),null);
var z = cljs.core.nth.call(null,vec__31024,(2),null);
var w = cljs.core.nth.call(null,vec__31024,(3),null);
var v = vec__31024;
var ___$1 = this;
if(cljs.core.truth_(w)){
return new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [((((x * self__.m00) + (y * self__.m10)) + (z * self__.m20)) + (w * self__.m30)),((((x * self__.m01) + (y * self__.m11)) + (z * self__.m21)) + (w * self__.m31)),((((x * self__.m02) + (y * self__.m12)) + (z * self__.m22)) + (w * self__.m32)),((((x * self__.m03) + (y * self__.m13)) + (z * self__.m23)) + (w * self__.m33))], null);
} else {
var b = (new Float32Array((3)));
(b[(0)] = ((((x * self__.m00) + (y * self__.m10)) + (z * self__.m20)) + self__.m30));

(b[(1)] = ((((x * self__.m01) + (y * self__.m11)) + (z * self__.m21)) + self__.m31));

(b[(2)] = ((((x * self__.m02) + (y * self__.m12)) + (z * self__.m22)) + self__.m32));

return (new thi.ng.geom.core.vector.Vec3(b,null,cljs.core.meta.call(null,v)));
}
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTransform$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTransform$transform$arity$2 = (function (_,matrix){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core._STAR_.call(null,___$1,matrix);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate$rotate$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
return thi.ng.geom.core.rotate_z.call(null,___$1,theta);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTranspose$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTranspose$transpose$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix44(self__.m00,self__.m10,self__.m20,self__.m30,self__.m01,self__.m11,self__.m21,self__.m31,self__.m02,self__.m12,self__.m22,self__.m32,self__.m03,self__.m13,self__.m23,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.toString = (function (){
var self__ = this;
var _ = this;
return cljs.core.apply.call(null,cljs.core.str,cljs.core.concat.call(null,"[",cljs.core.interpose.call(null," ",_),"]"));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate3D$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate3D$rotate_x$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
return (new thi.ng.geom.core.matrix.Matrix44(self__.m00,self__.m01,self__.m02,self__.m03,((self__.m10 * c) + (self__.m20 * s)),((self__.m11 * c) + (self__.m21 * s)),((self__.m12 * c) + (self__.m22 * s)),((self__.m13 * c) + (self__.m23 * s)),((self__.m20 * c) - (self__.m10 * s)),((self__.m21 * c) - (self__.m11 * s)),((self__.m22 * c) - (self__.m12 * s)),((self__.m23 * c) - (self__.m13 * s)),self__.m30,self__.m31,self__.m32,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate3D$rotate_y$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
return (new thi.ng.geom.core.matrix.Matrix44(((self__.m00 * c) - (self__.m20 * s)),((self__.m01 * c) - (self__.m21 * s)),((self__.m02 * c) - (self__.m22 * s)),((self__.m03 * c) - (self__.m23 * s)),self__.m10,self__.m11,self__.m12,self__.m13,((self__.m00 * s) + (self__.m20 * c)),((self__.m01 * s) + (self__.m21 * c)),((self__.m02 * s) + (self__.m22 * c)),((self__.m03 * s) + (self__.m23 * c)),self__.m30,self__.m31,self__.m32,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate3D$rotate_z$arity$2 = (function (_,theta){
var self__ = this;
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
return (new thi.ng.geom.core.matrix.Matrix44(((self__.m00 * c) + (self__.m10 * s)),((self__.m01 * c) + (self__.m11 * s)),((self__.m02 * c) + (self__.m12 * s)),((self__.m03 * c) + (self__.m13 * s)),((self__.m10 * c) - (self__.m00 * s)),((self__.m11 * c) - (self__.m01 * s)),((self__.m12 * c) - (self__.m02 * s)),((self__.m13 * c) - (self__.m03 * s)),self__.m20,self__.m21,self__.m22,self__.m23,self__.m30,self__.m31,self__.m32,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PRotate3D$rotate_around_axis$arity$3 = (function (_,p__31025,theta){
var self__ = this;
var vec__31026 = p__31025;
var x = cljs.core.nth.call(null,vec__31026,(0),null);
var y = cljs.core.nth.call(null,vec__31026,(1),null);
var z = cljs.core.nth.call(null,vec__31026,(2),null);
var ___$1 = this;
var s = Math.sin(theta);
var c = Math.cos(theta);
var sx = (s * x);
var sy = (s * y);
var sz = (s * z);
var t = (1.0 - c);
var tx = (t * x);
var ty = (t * y);
var b00 = ((tx * x) + c);
var b01 = ((tx * y) + sz);
var b02 = ((tx * z) - sy);
var b10 = ((ty * x) - sz);
var b11 = ((ty * y) + c);
var b12 = ((ty * z) + sx);
var b20 = ((tx * z) + sy);
var b21 = ((ty * z) - sx);
var b22 = (((t * z) * z) + c);
return (new thi.ng.geom.core.matrix.Matrix44((((self__.m00 * b00) + (self__.m10 * b01)) + (self__.m20 * b02)),(((self__.m01 * b00) + (self__.m11 * b01)) + (self__.m21 * b02)),(((self__.m02 * b00) + (self__.m12 * b01)) + (self__.m22 * b02)),(((self__.m03 * b00) + (self__.m13 * b01)) + (self__.m23 * b02)),(((self__.m00 * b10) + (self__.m10 * b11)) + (self__.m20 * b12)),(((self__.m01 * b10) + (self__.m11 * b11)) + (self__.m21 * b12)),(((self__.m02 * b10) + (self__.m12 * b11)) + (self__.m22 * b12)),(((self__.m03 * b10) + (self__.m13 * b11)) + (self__.m23 * b12)),(((self__.m00 * b20) + (self__.m10 * b21)) + (self__.m20 * b22)),(((self__.m01 * b20) + (self__.m11 * b21)) + (self__.m21 * b22)),(((self__.m02 * b20) + (self__.m12 * b21)) + (self__.m22 * b22)),(((self__.m03 * b20) + (self__.m13 * b21)) + (self__.m23 * b22)),self__.m30,self__.m31,self__.m32,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IIndexed$_nth$arity$2 = (function (_,k){
var self__ = this;
var ___$1 = this;
var G__31027 = (k | (0));
switch (G__31027) {
case (0):
return self__.m00;

break;
case (1):
return self__.m01;

break;
case (2):
return self__.m02;

break;
case (3):
return self__.m03;

break;
case (4):
return self__.m10;

break;
case (5):
return self__.m11;

break;
case (6):
return self__.m12;

break;
case (7):
return self__.m13;

break;
case (8):
return self__.m20;

break;
case (9):
return self__.m21;

break;
case (10):
return self__.m22;

break;
case (11):
return self__.m23;

break;
case (12):
return self__.m30;

break;
case (13):
return self__.m31;

break;
case (14):
return self__.m32;

break;
case (15):
return self__.m33;

break;
default:
return thi.ng.common.error.illegal_arg_BANG_.call(null,k);

}
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IIndexed$_nth$arity$3 = (function (_,k,nf){
var self__ = this;
var ___$1 = this;
var G__31028 = (k | (0));
switch (G__31028) {
case (0):
return self__.m00;

break;
case (1):
return self__.m01;

break;
case (2):
return self__.m02;

break;
case (3):
return self__.m03;

break;
case (4):
return self__.m10;

break;
case (5):
return self__.m11;

break;
case (6):
return self__.m12;

break;
case (7):
return self__.m13;

break;
case (8):
return self__.m20;

break;
case (9):
return self__.m21;

break;
case (10):
return self__.m22;

break;
case (11):
return self__.m23;

break;
case (12):
return self__.m30;

break;
case (13):
return self__.m31;

break;
case (14):
return self__.m32;

break;
case (15):
return self__.m33;

break;
default:
return nf;

}
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IMeta$_meta$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__._meta;
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$INext$_next$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 15, 5, cljs.core.PersistentVector.EMPTY_NODE, [self__.m01,self__.m02,self__.m03,self__.m10,self__.m11,self__.m12,self__.m13,self__.m20,self__.m21,self__.m22,self__.m23,self__.m30,self__.m31,self__.m32,self__.m33], null));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$ICounted$_count$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return (16);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PDeterminant$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PDeterminant$determinant$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var b00 = ((self__.m00 * self__.m11) - (self__.m01 * self__.m10));
var b01 = ((self__.m00 * self__.m12) - (self__.m02 * self__.m10));
var b02 = ((self__.m00 * self__.m13) - (self__.m03 * self__.m10));
var b03 = ((self__.m01 * self__.m12) - (self__.m02 * self__.m11));
var b04 = ((self__.m01 * self__.m13) - (self__.m03 * self__.m11));
var b05 = ((self__.m02 * self__.m13) - (self__.m03 * self__.m12));
var b06 = ((self__.m20 * self__.m31) - (self__.m21 * self__.m30));
var b07 = ((self__.m20 * self__.m32) - (self__.m22 * self__.m30));
var b08 = ((self__.m20 * self__.m33) - (self__.m23 * self__.m30));
var b09 = ((self__.m21 * self__.m32) - (self__.m22 * self__.m31));
var b10 = ((self__.m21 * self__.m33) - (self__.m23 * self__.m31));
var b11 = ((self__.m22 * self__.m33) - (self__.m23 * self__.m32));
return ((((b00 * b11) - (b01 * b10)) - (b04 * b07)) + (((b02 * b09) + (b03 * b08)) + (b05 * b06)));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IHash$_hash$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var or__16069__auto__ = self__._hasheq;
if(cljs.core.truth_(or__16069__auto__)){
return or__16069__auto__;
} else {
return self__._hasheq = cljs.core.hash_ordered_coll.call(null,___$1);
}
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IEquiv$_equiv$arity$2 = (function (_,o){
var self__ = this;
var ___$1 = this;
return (cljs.core.sequential_QMARK_.call(null,o)) && (((16) === cljs.core.count.call(null,o))) && (cljs.core.every_QMARK_.call(null,((function (___$1){
return (function (p1__31022_SHARP_){
return cljs.core._EQ_.call(null,p1__31022_SHARP_.call(null,(0)),p1__31022_SHARP_.call(null,(1)));
});})(___$1))
,cljs.core.map.call(null,cljs.core.vector,___$1,o)));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PInvert$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PInvert$invert$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
var n00 = ((self__.m00 * self__.m11) - (self__.m01 * self__.m10));
var n01 = ((self__.m00 * self__.m12) - (self__.m02 * self__.m10));
var n02 = ((self__.m00 * self__.m13) - (self__.m03 * self__.m10));
var n03 = ((self__.m01 * self__.m12) - (self__.m02 * self__.m11));
var n04 = ((self__.m01 * self__.m13) - (self__.m03 * self__.m11));
var n05 = ((self__.m02 * self__.m13) - (self__.m03 * self__.m12));
var n06 = ((self__.m20 * self__.m31) - (self__.m21 * self__.m30));
var n07 = ((self__.m20 * self__.m32) - (self__.m22 * self__.m30));
var n08 = ((self__.m20 * self__.m33) - (self__.m23 * self__.m30));
var n09 = ((self__.m21 * self__.m32) - (self__.m22 * self__.m31));
var n10 = ((self__.m21 * self__.m33) - (self__.m23 * self__.m31));
var n11 = ((self__.m22 * self__.m33) - (self__.m23 * self__.m32));
var d = ((((n00 * n11) - (n01 * n10)) - (n04 * n07)) + (((n02 * n09) + (n03 * n08)) + (n05 * n06)));
if((d === (0))){
return null;
} else {
var invd = (1.0 / d);
return (new thi.ng.geom.core.matrix.Matrix44(((((self__.m11 * n11) - (self__.m12 * n10)) + (self__.m13 * n09)) * invd),((((self__.m02 * n10) - (self__.m03 * n09)) + ((- self__.m01) * n11)) * invd),((((self__.m31 * n05) - (self__.m32 * n04)) + (self__.m33 * n03)) * invd),((((self__.m22 * n04) - (self__.m23 * n03)) + ((- self__.m21) * n05)) * invd),((((self__.m12 * n08) - (self__.m13 * n07)) + ((- self__.m10) * n11)) * invd),((((self__.m00 * n11) - (self__.m02 * n08)) + (self__.m03 * n07)) * invd),((((self__.m32 * n02) - (self__.m33 * n01)) + ((- self__.m30) * n05)) * invd),((((self__.m20 * n05) - (self__.m22 * n02)) + (self__.m23 * n01)) * invd),((((self__.m10 * n10) - (self__.m11 * n08)) + (self__.m13 * n06)) * invd),((((self__.m01 * n08) - (self__.m03 * n06)) + ((- self__.m00) * n10)) * invd),((((self__.m30 * n04) - (self__.m31 * n02)) + (self__.m33 * n00)) * invd),((((self__.m21 * n02) - (self__.m23 * n00)) + ((- self__.m20) * n04)) * invd),((((self__.m11 * n07) - (self__.m12 * n06)) + ((- self__.m10) * n09)) * invd),((((self__.m00 * n09) - (self__.m01 * n07)) + (self__.m02 * n06)) * invd),((((self__.m31 * n01) - (self__.m32 * n00)) + ((- self__.m30) * n03)) * invd),((((self__.m20 * n03) - (self__.m21 * n01)) + (self__.m22 * n00)) * invd),null,self__._meta));
}
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PMathOps$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PMathOps$_PLUS_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix44((self__.m00 + m__$1.m00),(self__.m01 + m__$1.m01),(self__.m02 + m__$1.m02),(self__.m03 + m__$1.m03),(self__.m10 + m__$1.m10),(self__.m11 + m__$1.m11),(self__.m12 + m__$1.m12),(self__.m13 + m__$1.m13),(self__.m20 + m__$1.m20),(self__.m21 + m__$1.m21),(self__.m22 + m__$1.m22),(self__.m23 + m__$1.m23),(self__.m30 + m__$1.m30),(self__.m31 + m__$1.m31),(self__.m32 + m__$1.m32),(self__.m33 + m__$1.m33),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PMathOps$_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix44((self__.m00 - m__$1.m00),(self__.m01 - m__$1.m01),(self__.m02 - m__$1.m02),(self__.m03 - m__$1.m03),(self__.m10 - m__$1.m10),(self__.m11 - m__$1.m11),(self__.m12 - m__$1.m12),(self__.m13 - m__$1.m13),(self__.m20 - m__$1.m20),(self__.m21 - m__$1.m21),(self__.m22 - m__$1.m22),(self__.m23 - m__$1.m23),(self__.m30 - m__$1.m30),(self__.m31 - m__$1.m31),(self__.m32 - m__$1.m32),(self__.m33 - m__$1.m33),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PMathOps$_STAR_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
var m__$1 = m;
return (new thi.ng.geom.core.matrix.Matrix44(((((self__.m00 * m__$1.m00) + (self__.m10 * m__$1.m01)) + (self__.m20 * m__$1.m02)) + (self__.m30 * m__$1.m03)),((((self__.m01 * m__$1.m00) + (self__.m11 * m__$1.m01)) + (self__.m21 * m__$1.m02)) + (self__.m31 * m__$1.m03)),((((self__.m02 * m__$1.m00) + (self__.m12 * m__$1.m01)) + (self__.m22 * m__$1.m02)) + (self__.m32 * m__$1.m03)),((((self__.m03 * m__$1.m00) + (self__.m13 * m__$1.m01)) + (self__.m23 * m__$1.m02)) + (self__.m33 * m__$1.m03)),((((self__.m00 * m__$1.m10) + (self__.m10 * m__$1.m11)) + (self__.m20 * m__$1.m12)) + (self__.m30 * m__$1.m13)),((((self__.m01 * m__$1.m10) + (self__.m11 * m__$1.m11)) + (self__.m21 * m__$1.m12)) + (self__.m31 * m__$1.m13)),((((self__.m02 * m__$1.m10) + (self__.m12 * m__$1.m11)) + (self__.m22 * m__$1.m12)) + (self__.m32 * m__$1.m13)),((((self__.m03 * m__$1.m10) + (self__.m13 * m__$1.m11)) + (self__.m23 * m__$1.m12)) + (self__.m33 * m__$1.m13)),((((self__.m00 * m__$1.m20) + (self__.m10 * m__$1.m21)) + (self__.m20 * m__$1.m22)) + (self__.m30 * m__$1.m23)),((((self__.m01 * m__$1.m20) + (self__.m11 * m__$1.m21)) + (self__.m21 * m__$1.m22)) + (self__.m31 * m__$1.m23)),((((self__.m02 * m__$1.m20) + (self__.m12 * m__$1.m21)) + (self__.m22 * m__$1.m22)) + (self__.m32 * m__$1.m23)),((((self__.m03 * m__$1.m20) + (self__.m13 * m__$1.m21)) + (self__.m23 * m__$1.m22)) + (self__.m33 * m__$1.m23)),((((self__.m00 * m__$1.m30) + (self__.m10 * m__$1.m31)) + (self__.m20 * m__$1.m32)) + (self__.m30 * m__$1.m33)),((((self__.m01 * m__$1.m30) + (self__.m11 * m__$1.m31)) + (self__.m21 * m__$1.m32)) + (self__.m31 * m__$1.m33)),((((self__.m02 * m__$1.m30) + (self__.m12 * m__$1.m31)) + (self__.m22 * m__$1.m32)) + (self__.m32 * m__$1.m33)),((((self__.m03 * m__$1.m30) + (self__.m13 * m__$1.m31)) + (self__.m23 * m__$1.m32)) + (self__.m33 * m__$1.m33)),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$ISeq$_rest$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return cljs.core.next.call(null,___$1);
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$ISeq$_first$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return self__.m00;
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTranslate$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTranslate$translate$arity$2 = (function (_,t){
var self__ = this;
var ___$1 = this;
var vec__31029 = ((typeof t === 'number')?new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [t,t,t], null):t);
var x = cljs.core.nth.call(null,vec__31029,(0),null);
var y = cljs.core.nth.call(null,vec__31029,(1),null);
var z = cljs.core.nth.call(null,vec__31029,(2),null);
return thi.ng.geom.core.translate.call(null,___$1,x,y,z);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PTranslate$translate$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix44(self__.m00,self__.m01,self__.m02,self__.m03,self__.m10,self__.m11,self__.m12,self__.m13,self__.m20,self__.m21,self__.m22,self__.m23,((((self__.m00 * x) + (self__.m10 * y)) + (self__.m20 * z)) + self__.m30),((((self__.m01 * x) + (self__.m11 * y)) + (self__.m21 * z)) + self__.m31),((((self__.m02 * x) + (self__.m12 * y)) + (self__.m22 * z)) + self__.m32),((((self__.m03 * x) + (self__.m13 * y)) + (self__.m23 * z)) + self__.m33),null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$ISeqable$_seq$arity$1 = (function (_){
var self__ = this;
var ___$1 = this;
return ___$1;
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PScale$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PScale$scale$arity$2 = (function (_,s){
var self__ = this;
var ___$1 = this;
var vec__31030 = ((typeof s === 'number')?new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [s,s,s], null):s);
var x = cljs.core.nth.call(null,vec__31030,(0),null);
var y = cljs.core.nth.call(null,vec__31030,(1),null);
var z = cljs.core.nth.call(null,vec__31030,(2),null);
return thi.ng.geom.core.scale.call(null,___$1,x,y,z);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$geom$core$PScale$scale$arity$4 = (function (_,x,y,z){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix44((self__.m00 * x),(self__.m01 * x),(self__.m02 * x),(self__.m03 * x),(self__.m10 * y),(self__.m11 * y),(self__.m12 * y),(self__.m13 * y),(self__.m20 * z),(self__.m21 * z),(self__.m22 * z),(self__.m23 * z),self__.m30,self__.m31,self__.m32,self__.m33,null,self__._meta));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return (new thi.ng.geom.core.matrix.Matrix44(self__.m00,self__.m01,self__.m02,self__.m03,self__.m10,self__.m11,self__.m12,self__.m13,self__.m20,self__.m21,self__.m22,self__.m23,self__.m30,self__.m31,self__.m32,self__.m33,self__._hasheq,m));
});

thi.ng.geom.core.matrix.Matrix44.prototype.cljs$core$ICollection$_conj$arity$2 = (function (_,x){
var self__ = this;
var ___$1 = this;
return new cljs.core.PersistentVector(null, 17, 5, cljs.core.PersistentVector.EMPTY_NODE, [self__.m00,self__.m01,self__.m02,self__.m03,self__.m10,self__.m11,self__.m12,self__.m13,self__.m20,self__.m21,self__.m22,self__.m23,self__.m30,self__.m31,self__.m32,self__.m33,x], null);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$math$core$PDeltaEquals$ = true;

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$2 = (function (_,m){
var self__ = this;
var ___$1 = this;
return thi.ng.math.core.delta_EQ_.call(null,___$1,m,thi.ng.math.core._STAR_eps_STAR_);
});

thi.ng.geom.core.matrix.Matrix44.prototype.thi$ng$math$core$PDeltaEquals$delta_EQ_$arity$3 = (function (_,m,eps){
var self__ = this;
var ___$1 = this;
var and__16057__auto__ = cljs.core.sequential_QMARK_.call(null,m);
if(and__16057__auto__){
var and__16057__auto____$1 = ((16) === cljs.core.count.call(null,m));
if(and__16057__auto____$1){
return thi.ng.math.core.delta_EQ_.call(null,cljs.core.into.call(null,cljs.core.PersistentVector.EMPTY,___$1),m,eps);
} else {
return and__16057__auto____$1;
}
} else {
return and__16057__auto__;
}
});

thi.ng.geom.core.matrix.Matrix44.getBasis = (function (){
return new cljs.core.PersistentVector(null, 18, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"m00","m00",236909271,null),new cljs.core.Symbol(null,"m01","m01",-1573844190,null),new cljs.core.Symbol(null,"m02","m02",1667378534,null),new cljs.core.Symbol(null,"m03","m03",-1771613944,null),new cljs.core.Symbol(null,"m10","m10",930266779,null),new cljs.core.Symbol(null,"m11","m11",-106606220,null),new cljs.core.Symbol(null,"m12","m12",405722345,null),new cljs.core.Symbol(null,"m13","m13",446740010,null),new cljs.core.Symbol(null,"m20","m20",893570591,null),new cljs.core.Symbol(null,"m21","m21",-832208920,null),new cljs.core.Symbol(null,"m22","m22",-939822876,null),new cljs.core.Symbol(null,"m23","m23",-565099055,null),new cljs.core.Symbol(null,"m30","m30",-890772748,null),new cljs.core.Symbol(null,"m31","m31",961901948,null),new cljs.core.Symbol(null,"m32","m32",-1947713286,null),new cljs.core.Symbol(null,"m33","m33",-1504718133,null),new cljs.core.Symbol(null,"_hasheq","_hasheq",-1509801429,null),new cljs.core.Symbol(null,"_meta","_meta",-1716892533,null)], null);
});

thi.ng.geom.core.matrix.Matrix44.cljs$lang$type = true;

thi.ng.geom.core.matrix.Matrix44.cljs$lang$ctorStr = "thi.ng.geom.core.matrix/Matrix44";

thi.ng.geom.core.matrix.Matrix44.cljs$lang$ctorPrWriter = (function (this__16648__auto__,writer__16649__auto__,opt__16650__auto__){
return cljs.core._write.call(null,writer__16649__auto__,"thi.ng.geom.core.matrix/Matrix44");
});

thi.ng.geom.core.matrix.__GT_Matrix44 = (function thi$ng$geom$core$matrix$__GT_Matrix44(m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33,_hasheq,_meta){
return (new thi.ng.geom.core.matrix.Matrix44(m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33,_hasheq,_meta));
});

thi.ng.geom.core.matrix.M32 = (new thi.ng.geom.core.matrix.Matrix32(1.0,0.0,0.0,0.0,1.0,0.0,null,null));
thi.ng.geom.core.matrix.M44 = (new thi.ng.geom.core.matrix.Matrix44(1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,null,null));
thi.ng.geom.core.matrix.matrix32 = (function thi$ng$geom$core$matrix$matrix32(){
var G__31034 = arguments.length;
switch (G__31034) {
case 0:
return thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 6:
return thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$6((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.matrix.M32;
});

thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$1 = (function (p__31035){
var vec__31036 = p__31035;
var m00 = cljs.core.nth.call(null,vec__31036,(0),null);
var m01 = cljs.core.nth.call(null,vec__31036,(1),null);
var m02 = cljs.core.nth.call(null,vec__31036,(2),null);
var m10 = cljs.core.nth.call(null,vec__31036,(3),null);
var m11 = cljs.core.nth.call(null,vec__31036,(4),null);
var m12 = cljs.core.nth.call(null,vec__31036,(5),null);
return (new thi.ng.geom.core.matrix.Matrix32(m00,m01,m02,m10,m11,m12,null,null));
});

thi.ng.geom.core.matrix.matrix32.cljs$core$IFn$_invoke$arity$6 = (function (m00,m01,m02,m10,m11,m12){
return (new thi.ng.geom.core.matrix.Matrix32(m00,m01,m02,m10,m11,m12,null,null));
});

thi.ng.geom.core.matrix.matrix32.cljs$lang$maxFixedArity = 6;
thi.ng.geom.core.matrix.matrix44 = (function thi$ng$geom$core$matrix$matrix44(){
var G__31039 = arguments.length;
switch (G__31039) {
case 0:
return thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$0();

break;
case 1:
return thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case 16:
return thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$16((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]),(arguments[(6)]),(arguments[(7)]),(arguments[(8)]),(arguments[(9)]),(arguments[(10)]),(arguments[(11)]),(arguments[(12)]),(arguments[(13)]),(arguments[(14)]),(arguments[(15)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$0 = (function (){
return thi.ng.geom.core.matrix.M44;
});

thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$1 = (function (p__31040){
var vec__31041 = p__31040;
var m00 = cljs.core.nth.call(null,vec__31041,(0),null);
var m01 = cljs.core.nth.call(null,vec__31041,(1),null);
var m02 = cljs.core.nth.call(null,vec__31041,(2),null);
var m03 = cljs.core.nth.call(null,vec__31041,(3),null);
var m10 = cljs.core.nth.call(null,vec__31041,(4),null);
var m11 = cljs.core.nth.call(null,vec__31041,(5),null);
var m12 = cljs.core.nth.call(null,vec__31041,(6),null);
var m13 = cljs.core.nth.call(null,vec__31041,(7),null);
var m20 = cljs.core.nth.call(null,vec__31041,(8),null);
var m21 = cljs.core.nth.call(null,vec__31041,(9),null);
var m22 = cljs.core.nth.call(null,vec__31041,(10),null);
var m23 = cljs.core.nth.call(null,vec__31041,(11),null);
var m30 = cljs.core.nth.call(null,vec__31041,(12),null);
var m31 = cljs.core.nth.call(null,vec__31041,(13),null);
var m32 = cljs.core.nth.call(null,vec__31041,(14),null);
var m33 = cljs.core.nth.call(null,vec__31041,(15),null);
return (new thi.ng.geom.core.matrix.Matrix44(m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33,null,null));
});

thi.ng.geom.core.matrix.matrix44.cljs$core$IFn$_invoke$arity$16 = (function (m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33){
return (new thi.ng.geom.core.matrix.Matrix44(m00,m01,m02,m03,m10,m11,m12,m13,m20,m21,m22,m23,m30,m31,m32,m33,null,null));
});

thi.ng.geom.core.matrix.matrix44.cljs$lang$maxFixedArity = 16;
thi.ng.geom.core.matrix.matrix44__GT_matrix33 = (function thi$ng$geom$core$matrix$matrix44__GT_matrix33(p__31043){
var vec__31045 = p__31043;
var m00 = cljs.core.nth.call(null,vec__31045,(0),null);
var m01 = cljs.core.nth.call(null,vec__31045,(1),null);
var m02 = cljs.core.nth.call(null,vec__31045,(2),null);
var _ = cljs.core.nth.call(null,vec__31045,(3),null);
var m10 = cljs.core.nth.call(null,vec__31045,(4),null);
var m11 = cljs.core.nth.call(null,vec__31045,(5),null);
var m12 = cljs.core.nth.call(null,vec__31045,(6),null);
var ___$1 = cljs.core.nth.call(null,vec__31045,(7),null);
var m20 = cljs.core.nth.call(null,vec__31045,(8),null);
var m21 = cljs.core.nth.call(null,vec__31045,(9),null);
var m22 = cljs.core.nth.call(null,vec__31045,(10),null);
return new cljs.core.PersistentVector(null, 9, 5, cljs.core.PersistentVector.EMPTY_NODE, [m00,m01,m02,m10,m11,m12,m20,m21,m22], null);
});
thi.ng.geom.core.matrix.matrix44__GT_matrix33_rot = (function thi$ng$geom$core$matrix$matrix44__GT_matrix33_rot(p__31046){
var vec__31048 = p__31046;
var m00 = cljs.core.nth.call(null,vec__31048,(0),null);
var m01 = cljs.core.nth.call(null,vec__31048,(1),null);
var m02 = cljs.core.nth.call(null,vec__31048,(2),null);
var _ = cljs.core.nth.call(null,vec__31048,(3),null);
var m10 = cljs.core.nth.call(null,vec__31048,(4),null);
var m11 = cljs.core.nth.call(null,vec__31048,(5),null);
var m12 = cljs.core.nth.call(null,vec__31048,(6),null);
var ___$1 = cljs.core.nth.call(null,vec__31048,(7),null);
var m20 = cljs.core.nth.call(null,vec__31048,(8),null);
var m21 = cljs.core.nth.call(null,vec__31048,(9),null);
var m22 = cljs.core.nth.call(null,vec__31048,(10),null);
var b01 = ((m22 * m11) - (m12 * m21));
var b11 = ((m12 * m20) - (m22 * m10));
var b21 = ((m21 * m10) - (m11 * m20));
var invd = ((1) / (((m00 * b01) + (m01 * b11)) + (m02 * b21)));
return new cljs.core.PersistentVector(null, 9, 5, cljs.core.PersistentVector.EMPTY_NODE, [(b01 * invd),(b11 * invd),(b21 * invd),(((m02 * m21) - (m22 * m01)) * invd),(((m22 * m00) - (m02 * m20)) * invd),(((m01 * m20) - (m21 * m00)) * invd),(((m12 * m01) - (m02 * m11)) * invd),(((m02 * m10) - (m12 * m00)) * invd),(((m11 * m00) - (m01 * m10)) * invd)], null);
});
/**
 * Sets up a viewing frustum, shaped like a truncated pyramid with the
 * camera where the tip of the pyramid would be.
 * This emulates the OpenGL function glFrustum().
 */
thi.ng.geom.core.matrix.frustum = (function thi$ng$geom$core$matrix$frustum(l,t,r,b,n,f){
var dx = ((1) / (r - l));
var dy = ((1) / (t - b));
var dz = ((1) / (n - f));
var n2 = (2.0 * n);
return (new thi.ng.geom.core.matrix.Matrix44((n2 * dx),0.0,0.0,0.0,0.0,(n2 * dy),0.0,0.0,((l + r) * dx),((t + b) * dy),((n + f) * dz),-1.0,0.0,0.0,((n2 * f) * dz),0.0,null,null));
});
/**
 * Given vertical FOV in degrees, aspect ratio and near plane
 * distance, computes map of left/right/top/bottom view frustum
 * bounds.
 */
thi.ng.geom.core.matrix.frustum_bounds = (function thi$ng$geom$core$matrix$frustum_bounds(fovy,aspect,near){
var top = (near * Math.tan((0.5 * thi.ng.math.core.radians.call(null,fovy))));
var right = (top * aspect);
return new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"left","left",-399115937),(- right),new cljs.core.Keyword(null,"right","right",-452581833),right,new cljs.core.Keyword(null,"top","top",-1856271961),top,new cljs.core.Keyword(null,"bottom","bottom",-1550509018),(- top)], null);
});
/**
 * Given a view matrix & projection matrix, returns vector of the
 * frustum's 6 plane parameters, each a 2-element vector: [normal w]
 * Planes are ordered: left, right, top, bottom, near, far. These
 * coefficients can then (for example) be used for AABB-frustum culling
 * with thi.nggeom.core.intersect/intersect-aabb-frustum?
 */
thi.ng.geom.core.matrix.frustum_planes = (function thi$ng$geom$core$matrix$frustum_planes(view,proj){
var pv = thi.ng.geom.core.transpose.call(null,thi.ng.geom.core._STAR_.call(null,proj,view));
var m30 = (- pv.m30);
var m31 = (- pv.m31);
var m32 = (- pv.m32);
var m33 = (- pv.m33);
return new cljs.core.PersistentVector(null, 6, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 - pv.m00),(m31 - pv.m01),(m32 - pv.m02)),(m33 - pv.m03)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 + pv.m00),(m31 + pv.m01),(m32 + pv.m02)),(m33 + pv.m03)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 - pv.m10),(m31 - pv.m11),(m32 - pv.m12)),(m33 - pv.m13)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 + pv.m10),(m31 + pv.m11),(m32 + pv.m12)),(m33 + pv.m13)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 - pv.m20),(m31 - pv.m21),(m32 - pv.m22)),(m33 - pv.m23)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [thi.ng.geom.core.vector.vec3.call(null,(m30 + pv.m20),(m31 + pv.m21),(m32 + pv.m22)),(m33 + pv.m23)], null)], null);
});
/**
 * Returns an orthographic projection matrix, in which objects are the
 * same size no matter how far away or nearby they are. This emulates
 * the OpenGL function glOrtho().
 */
thi.ng.geom.core.matrix.ortho = (function thi$ng$geom$core$matrix$ortho(l,t,r,b,n,f){
var dx = ((1) / (l - r));
var dy = ((1) / (b - t));
var dz = ((1) / (n - f));
return (new thi.ng.geom.core.matrix.Matrix44((-2.0 * dx),0.0,0.0,0.0,0.0,(-2.0 * dy),0.0,0.0,0.0,0.0,(2.0 * dz),0.0,((l + r) * dx),((t + b) * dy),((n + f) * dz),1.0,null,null));
});
/**
 * Returns a perspective transform matrix, which makes far away objects appear
 * smaller than nearby objects. The `aspect` argument should be the width
 * divided by the height of your viewport and `fov` is the vertical angle
 * of the field of view in degrees.
 */
thi.ng.geom.core.matrix.perspective = (function thi$ng$geom$core$matrix$perspective(fovy,aspect,near,far){
var f = ((1) / Math.tan((0.5 * thi.ng.math.core.radians.call(null,fovy))));
var nf = ((1) / (near - far));
return (new thi.ng.geom.core.matrix.Matrix44((f / aspect),0.0,0.0,0.0,0.0,f,0.0,0.0,0.0,0.0,((near + far) * nf),-1.0,0.0,0.0,(((2.0 * near) * far) * nf),0.0,null,null));
});
thi.ng.geom.core.matrix.perspective_frustum = (function thi$ng$geom$core$matrix$perspective_frustum(fov,aspect,near,far){
var map__31050 = thi.ng.geom.core.matrix.frustum_bounds.call(null,fov,aspect,near);
var map__31050__$1 = ((cljs.core.seq_QMARK_.call(null,map__31050))?cljs.core.apply.call(null,cljs.core.hash_map,map__31050):map__31050);
var left = cljs.core.get.call(null,map__31050__$1,new cljs.core.Keyword(null,"left","left",-399115937));
var right = cljs.core.get.call(null,map__31050__$1,new cljs.core.Keyword(null,"right","right",-452581833));
var top = cljs.core.get.call(null,map__31050__$1,new cljs.core.Keyword(null,"top","top",-1856271961));
var bottom = cljs.core.get.call(null,map__31050__$1,new cljs.core.Keyword(null,"bottom","bottom",-1550509018));
return thi.ng.geom.core.matrix.frustum.call(null,left,top,right,bottom,near,far);
});
/**
 * Takes 6 numbers representing eye & target positions, computes up
 * vector and returns vector of all three vec3's.
 */
thi.ng.geom.core.matrix.look_at_vectors = (function thi$ng$geom$core$matrix$look_at_vectors(ex,ey,ez,tx,ty,tz){
var eye = thi.ng.geom.core.vector.vec3.call(null,ex,ey,ez);
var target = thi.ng.geom.core.vector.vec3.call(null,tx,ty,tz);
var up = thi.ng.geom.core.utils.ortho_normal.call(null,thi.ng.geom.core._.call(null,eye,target),thi.ng.geom.core.vector.V3X);
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [eye,target,up], null);
});
/**
 * Returns a matrix that puts the camera at the eye position looking
 * toward the target point with the given up direction.
 */
thi.ng.geom.core.matrix.look_at = (function thi$ng$geom$core$matrix$look_at(eye,target,up){
var dir = thi.ng.geom.core._.call(null,eye,target);
if(cljs.core.truth_(thi.ng.math.core.delta_EQ_.call(null,thi.ng.geom.core.vector.V3,dir))){
return thi.ng.geom.core.matrix.M44;
} else {
var vec__31054 = thi.ng.geom.core.normalize.call(null,dir);
var zx = cljs.core.nth.call(null,vec__31054,(0),null);
var zy = cljs.core.nth.call(null,vec__31054,(1),null);
var zz = cljs.core.nth.call(null,vec__31054,(2),null);
var z = vec__31054;
var vec__31055 = thi.ng.geom.core.utils.ortho_normal.call(null,up,z);
var xx = cljs.core.nth.call(null,vec__31055,(0),null);
var xy = cljs.core.nth.call(null,vec__31055,(1),null);
var xz = cljs.core.nth.call(null,vec__31055,(2),null);
var x = vec__31055;
var vec__31056 = thi.ng.geom.core.utils.ortho_normal.call(null,z,x);
var yx = cljs.core.nth.call(null,vec__31056,(0),null);
var yy = cljs.core.nth.call(null,vec__31056,(1),null);
var yz = cljs.core.nth.call(null,vec__31056,(2),null);
var y = vec__31056;
return (new thi.ng.geom.core.matrix.Matrix44(xx,yx,zx,0.0,xy,yy,zy,0.0,xz,yz,zz,0.0,(- thi.ng.geom.core.dot.call(null,x,eye)),(- thi.ng.geom.core.dot.call(null,y,eye)),(- thi.ng.geom.core.dot.call(null,z,eye)),1.0,null,null));
}
});
/**
 * Given viewport width/height, computes a 2D transformation matrix
 * mapping normalized coordinates to screen space. If invert-y? is
 * true, the Y axis is flipped.
 */
thi.ng.geom.core.matrix.viewport_matrix = (function thi$ng$geom$core$matrix$viewport_matrix(){
var G__31058 = arguments.length;
switch (G__31058) {
case 2:
return thi.ng.geom.core.matrix.viewport_matrix.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case 3:
return thi.ng.geom.core.matrix.viewport_matrix.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.matrix.viewport_matrix.cljs$core$IFn$_invoke$arity$2 = (function (width,height){
return thi.ng.geom.core.matrix.viewport_matrix.call(null,width,height,false);
});

thi.ng.geom.core.matrix.viewport_matrix.cljs$core$IFn$_invoke$arity$3 = (function (width,height,invert_y_QMARK_){
var w2 = (width / 2.0);
var h2 = (height / 2.0);
return thi.ng.geom.core._STAR_.call(null,thi.ng.geom.core.translate.call(null,thi.ng.geom.core.matrix.M32,w2,h2),thi.ng.geom.core.scale.call(null,thi.ng.geom.core.matrix.M32,w2,(cljs.core.truth_(invert_y_QMARK_)?h2:(- h2))));
});

thi.ng.geom.core.matrix.viewport_matrix.cljs$lang$maxFixedArity = 3;
/**
 * Projects 3D point p using MVP matrix into 2D and the applies
 * viewport matrix to produce screen coordinate.
 */
thi.ng.geom.core.matrix.project_point = (function thi$ng$geom$core$matrix$project_point(p,mvp,vtx){
var vec__31061 = thi.ng.geom.core.transform_vector.call(null,mvp,cljs.core.conj.call(null,thi.ng.geom.core.vector.vec3.call(null,p),(1)));
var x = cljs.core.nth.call(null,vec__31061,(0),null);
var y = cljs.core.nth.call(null,vec__31061,(1),null);
var _ = cljs.core.nth.call(null,vec__31061,(2),null);
var w = cljs.core.nth.call(null,vec__31061,(3),null);
return thi.ng.geom.core.transform_vector.call(null,vtx,thi.ng.geom.core.div.call(null,thi.ng.geom.core.vector.vec2.call(null,x,y),w));
});
/**
 * Like project-point, but returns vec3 with z component representing
 * depth value.
 */
thi.ng.geom.core.matrix.project_point_z = (function thi$ng$geom$core$matrix$project_point_z(p,mvp,vtx){
var vec__31063 = thi.ng.geom.core.transform_vector.call(null,mvp,cljs.core.conj.call(null,thi.ng.geom.core.vector.vec3.call(null,p),(1)));
var x = cljs.core.nth.call(null,vec__31063,(0),null);
var y = cljs.core.nth.call(null,vec__31063,(1),null);
var z = cljs.core.nth.call(null,vec__31063,(2),null);
var w = cljs.core.nth.call(null,vec__31063,(3),null);
return thi.ng.geom.core.vector.vec3.call(null,thi.ng.geom.core.transform_vector.call(null,vtx,thi.ng.geom.core.div.call(null,thi.ng.geom.core.vector.vec2.call(null,x,y),w)),(z / w));
});
/**
 * Takes a vec3 in screenspace, view matrix, projection matrix and
 * screen rect. A second arity exists accepting an already inverted
 * view-projection matrix instead of having to supply view & proj
 * separately. Returns vector in world space or nil if matrix is not
 * invertible.
 */
thi.ng.geom.core.matrix.unproject_point = (function thi$ng$geom$core$matrix$unproject_point(){
var G__31065 = arguments.length;
switch (G__31065) {
case 4:
return thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$4((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]));

break;
case 3:
return thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
case 6:
return thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$6((arguments[(0)]),(arguments[(1)]),(arguments[(2)]),(arguments[(3)]),(arguments[(4)]),(arguments[(5)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(arguments.length)].join('')));

}
});

thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$4 = (function (p,view,proj,screen_rect){
var temp__4423__auto__ = thi.ng.geom.core.invert.call(null,thi.ng.geom.core._STAR_.call(null,proj,view));
if(cljs.core.truth_(temp__4423__auto__)){
var inv_mat = temp__4423__auto__;
return thi.ng.geom.core.matrix.unproject_point.call(null,p,inv_mat,screen_rect);
} else {
return null;
}
});

thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$3 = (function (p,inv_mat,p__31066){
var map__31067 = p__31066;
var map__31067__$1 = ((cljs.core.seq_QMARK_.call(null,map__31067))?cljs.core.apply.call(null,cljs.core.hash_map,map__31067):map__31067);
var vec__31068 = cljs.core.get.call(null,map__31067__$1,new cljs.core.Keyword(null,"p","p",151049309));
var vx = cljs.core.nth.call(null,vec__31068,(0),null);
var vy = cljs.core.nth.call(null,vec__31068,(1),null);
var vec__31069 = cljs.core.get.call(null,map__31067__$1,new cljs.core.Keyword(null,"size","size",1098693007));
var w = cljs.core.nth.call(null,vec__31069,(0),null);
var h = cljs.core.nth.call(null,vec__31069,(1),null);
return thi.ng.geom.core.matrix.unproject_point.call(null,p,inv_mat,vx,vy,w,h);
});

thi.ng.geom.core.matrix.unproject_point.cljs$core$IFn$_invoke$arity$6 = (function (p__31070,inv_mat,vx,vy,w,h){
var vec__31071 = p__31070;
var x = cljs.core.nth.call(null,vec__31071,(0),null);
var y = cljs.core.nth.call(null,vec__31071,(1),null);
var z = cljs.core.nth.call(null,vec__31071,(2),null);
var x_SINGLEQUOTE_ = (((2.0 * (x - vx)) / w) - (1));
var y_SINGLEQUOTE_ = (((2.0 * (((h - y) - (1)) - vy)) / h) - (1));
var z_SINGLEQUOTE_ = ((z * 2.0) - (1));
var p_SINGLEQUOTE_ = thi.ng.geom.core.transform_vector.call(null,inv_mat,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [x_SINGLEQUOTE_,y_SINGLEQUOTE_,z_SINGLEQUOTE_], null));
return thi.ng.geom.core._STAR_.call(null,p_SINGLEQUOTE_,((1) / ((((x_SINGLEQUOTE_ * inv_mat.m03) + (y_SINGLEQUOTE_ * inv_mat.m13)) + (z_SINGLEQUOTE_ * inv_mat.m23)) + inv_mat.m33)));
});

thi.ng.geom.core.matrix.unproject_point.cljs$lang$maxFixedArity = 6;

//# sourceMappingURL=matrix.js.map?rel=1439206035184