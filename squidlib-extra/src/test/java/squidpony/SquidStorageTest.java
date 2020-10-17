package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * Just a test.
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class SquidStorageTest extends ApplicationAdapter {
    public static class TestClass
    {
        public EnumMap<Direction, String> em = new EnumMap<>(Direction.class);
        public EnumOrderedMap<Direction, String> om = Maker.makeEOM(
                Direction.DOWN_LEFT, "California",
                Direction.DOWN_RIGHT, "Florida",
                Direction.UP_RIGHT, "Maine",
                Direction.UP_LEFT, "Washington",
                Direction.DOWN, "Texas");
        public EnumOrderedSet<Radius> radii = Maker.makeEOS(Radius.DIAMOND, Radius.CIRCLE, Radius.SQUARE);
        public TestClass()
        {
        }
        public void initialize()
        {
            em.put(Direction.DOWN_LEFT, "California");
            em.put(Direction.DOWN_RIGHT, "Florida");
            em.put(Direction.UP_RIGHT, "Maine");
            em.put(Direction.UP_LEFT, "Washington");
            em.put(Direction.DOWN, "Texas");
        }

        @Override
        public String toString() {
            return em.toString() + " vs. " + om.toString() + "; EnumOrderedSet should be Diamond, Circle, Square, and it is: " + radii.toString();
        }
    }
    @Override
    public void create() {
        super.create();
        if(false) {
            SquidStorage store = new SquidStorage("StorageTest", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            store.compress = true;
            System.out.println(store.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!"), r2;

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, FakeLanguageGen.ARABIC_ROMANIZED, 5, FakeLanguageGen.JAPANESE_ROMANIZED, 3), lang2;
            // with custom serializer, compresses to:
            //0#1384785347551869630@4.0~12@5.0~8@3.0
            // without custom serializer, compresses to:
            //"{openingVowels:[e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,ai,ou,e,u,a,a,i,a,o,u,i,o,ai,i,e,i,a,o,a,u,a,o,a,o,e,a,oa,o,i,ii,a,a,a,a,e,uu,u,uu,a,ae,a,a,a,i,a,a,i,a,aa,u,e,i,o,i,i,oo,ii,e,i,u,a,u,a,ii,uu,ii,aa,aa,e,uu,o,u,o,a,e,a,ii,u,o,u,o,o,ai,a,uu,aa,o,i,uu,i,a,a,u,i,a,e,ii,o,a,a,u,a,o,aa,u,uu,ii,e,a,au,o,a,e,e,u,uu,eo,a,a,e,u,o,o,e,i,ii,a,o,o,o,eo,o,e,a,eo,a,uu,e,a,a,a,e,a,ii,a,o,u,oo,u,u,e,ae,a,e,e,ai,i,o,i,a,a,aa,a,ai,o,i,a,oa,e,o,u,i,a,i,ii,o,e,o,e,e,a,i,u,a,u,a,a,a,e,eo,ii,e,e,aa,i,e,ae,i,i,aa,e,a,a,a,u,aa,i,u,e,a,u,a,u,eo,u,a,eo,uu,oa,ii,a,e,o,aa,ii,u,o,ai,e,a,a,aa,a,a,a,ai,a,u,i,a,e,e,a,a,e,a,a,e,a,e,a,a,u,i,o,i,a,i,a,e,u,a,oa,aa,ai,ii,a,e,i,aa,ii,uu,ea,i,ai,ii,u,i,e,a,ae,aa,i,a,u,a,i,i,i,e,u,o,i,i,a,i,i,a,i,a,a,o,uu,a,a,a,ii,i,e,uu,aa,i,ai,e,i,a,i,i,i,e,ii,o,au,i,a,uu,e,a,oo,e,ee,a,u,aa,a,aa,a,ii,e,aa,uu,a,a,eo,ii,a,u,o,u,a,u,e,i,a,o,e,o,i,i,o,a,i,a,a,a,u,u,o,ii,a,e,uu,u,e,a,a,ii,uu,uu,ai,o,aa,a,e,o,oa,ii,ai,i,o,a,i,u,ai,a,o,ii,i,e,i,o,a,e,a,u,a,i,e,o,a,ai,ea,aa,e,e,aa,aa,i,i,e,ii,a,e,a,aa,a,o,uu,a,o,a,a,i,a,e,ae,o,o,aa,i,ai,aa,uu,a,eo,a,o,i,ae,o,e,aa,o,o,i,o,i,a,a,u,a,oo,a,o,o,o,uu,a,i,a,u,i,u,o,a,au,i,a,a,a,a,a,e,a,e,e,a,e,a,u,aa,a,a,a,a,ai,i,e,e,u,uu,i,e,ii,i,ii,a,oo,e,aa,i,ii,a,o,a,o,o,e,a,u,o,a,au,o,e,ii,au,aa,a,o,a,eo,aa,a,a,a,u,a,aa,u,i,i,aa,ea,e,u,a,ii,aa,a,a,o,u,e,ii,i,a,u,a,u,oo,ii,ee,aa,i,i,a,aa,a,o,i,aa,oo,ee,e,a,aa,e,i,o,e,a,a,u,a,e,i,e,a,a,e,aa,ae,i,a,e,eo,u,e,oa,a,u,a,o,u,u,a,e,a,u,u,ea,e,o,ii,a,oo,e,a,i,i,u,o,i,i,e,u,e,o,ai,o,oa,o,e,aa,i,ii,a,a,i,aa,e,u,a,u,aa,a,o,aa,o,o,u,i,a,o,e,au,u,e,i,i,i,i,e,o,aa,o,a,aa,ii,au,uu,o,a,ii,e,a,i,o,a,a,ea,i,a,ii,o,e,o,aa,a,ii,a,a,ae,ii,ii,aa,u,aa,a,ii,o,a,e,eo,u,a,o,u,a,a,e,a,u,a,i,e,a,e,u,ii,a,o,e,u,o,a,o,aa,ai,uu,e,e,o,e,o,o,aa,aa,o,au,o,a,e,aa,o,e,uu,o,e,o,a,ii,ae,au,e,u,ee,a,a,oo,u,o,u,a,u,i,uu],midVowels:[ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,e,u,ai,a,i,o,o,ou,a,u,i,a,o,i,a,e,i,ai,aa,a,u,a,uu,u,o,u,i,aa,aa,a,u,eo,au,o,e,u,aa,o,au,oo,a,a,o,a,o,e,e,i,ii,i,ae,a,ii,ii,ii,uu,aa,ae,au,o,i,a,a,o,i,a,oo,a,a,a,aa,e,o,a,o,u,u,i,u,aa,o,a,a,i,i,e,i,a,aa,i,a,aa,i,o,o,u,e,e,u,i,uu,ii,a,aa,e,e,i,uu,i,aa,a,i,a,a,u,i,a,i,e,o,a,a,a,a,a,a,aa,ii,uu,uu,o,oo,e,ae,e,e,ea,e,u,o,o,e,u,o,e,e,i,i,u,a,uu,e,u,o,aa,o,a,a,oo,e,u,o,e,i,o,a,oa,a,a,a,aa,o,aa,o,a,ai,e,a,ea,ii,ii,aa,ai,a,a,a,oa,e,eo,ii,ai,u,a,e,o,o,u,o,a,a,e,i,a,ii,a,i,e,u,a,o,uu,e,i,e,a,i,a,i,u,oa,i,o,a,a,au,u,uu,a,e,a,i,e,eo,ii,i,o,i,o,aa,u,a,a,a,o,e,oo,o,e,i,a,i,e,o,uu,u,i,ii,i,a,a,ii,a,e,i,a,o,i,a,o,i,a,ii,i,e,a,o,u,u,u,a,i,i,a,a,au,a,ai,ee,ii,e,i,a,u,e,i,ai,a,e,i,aa,u,ae,i,a,ee,aa,e,e,o,i,u,o,o,a,ii,aa,ai,oa,e,i,ii,a,o,a,e,u,uu,ea,e,a,e,a,a,ii,aa,ai,aa,e,ii,au,e,ii,i,a,aa,e,i,o,e,i,ii,a,e,e,a,aa,i,a,o,u,o,a,ii,u,uu,aa,ii,a,i,e,uu,u,o,a,o,uu,a,e,o,e,u,a,u,e,e,o,a,u,aa,uu,a,uu,o,ii,e,i,u,e,u,a,i,au,e,a,eo,i,u,eo,e,e,e,a,u,e,a,e,uu,o,aa,a,o,o,o,ii,a,a,e,a,u,i,aa,a,e,o,e,i,o,o,aa,eo,ai,o,u,a,i,ii,a,e,a,a,u,e,ea,o,i,i,ii,u,i,e,u,ai,oa,e,e,e,e,a,a,e,a,ii,i,i,i,uu,uu,ae,a,a,aa,e,i,e,aa,a,e,e,o,i,u,aa,a,eo,e,a,uu,e,a,a,u,e,a,ii,a,a,uu,a,oo,aa,i,i,ai,ii,a,e,oo,i,o,au,ii,ii,aa,a,a,o,o,a,u,o,a,u,a,ai,a,a,a,a,ii,u,a,o,a,u,a,i,o,ai,i,e,o,o,uu,o,e,e,eo,i,e,i,i,a,a,a,e,a,a,ii,e,o,i,a,u,a,a,aa,uu,a,ii,u,a,i,aa,i,a,aa,a,a,u,o,a,uu,u,u,a,ii,uu,o,ai,a,aa,uu,i,e,o,aa,a,ai,u,ae,a,eo,u,o,e,o,i,a,aa,i,i,ea,ae,a,i,o,o,e,e,o,u,u,u,i,o,e,uu,a,a,a,u,a,e,a,a,ae,uu,o,e,a,o,a,aa,o,uu,a,e,i,a,a,a,i,a,uu,u,i,o,e,a,a,o,u,a,oo,o,a,aa,aa,a,e,a,a,u,ai,a,e,a,e,e,o,o,i,u,e,e,ii,a,a,uu,e,e,e,e,o,a,u,o,e,i,i,a,e,ea,o,ee,o,a,a,e,a,aa,ea,ii,aa,a,e,a,o,a,aa,a,a,a,a,a,au,o,i,i,u,u,u,i,e,a,a,a,u,i,uu,ee,o,ai,i,i,aa,i,a,au,o,oo,e,o,au,e,o,a,o],openingConsonants:[k,s,g,r,k,py,g,gy,n,hy,sh,b,by,sh,z,n,sh,ch,k,s,g,z,y,k,t,my,ts,t,b,k,ts,r,d,b,s,sh,t,k,t,ry,ry,k,t,sh,p,h,n,m,s,r,j,s,t,sh,f,d,b,ch,ny,n,sh,ky,t,s,s,k,s,g,r,k,py,g,gy,n,hy,sh,b,by,sh,z,n,sh,ch,k,s,g,z,y,k,t,my,ts,t,b,k,ts,r,d,b,s,sh,t,k,t,ry,ry,k,t,sh,p,h,n,m,s,r,j,s,t,sh,f,d,b,ch,ny,n,sh,ky,t,s,s,k,s,g,r,k,py,g,gy,n,hy,sh,b,by,sh,z,n,sh,ch,k,s,g,z,y,k,t,my,ts,t,b,k,ts,r,d,b,s,sh,t,k,t,ry,ry,k,t,sh,p,h,n,m,s,r,j,s,t,sh,f,d,b,ch,ny,n,sh,ky,t,s,s,k,s,g,r,k,py,g,gy,n,hy,sh,b,by,sh,z,n,sh,ch,k,s,g,z,y,k,t,my,ts,t,b,k,ts,r,d,b,s,sh,t,k,t,ry,ry,k,t,sh,p,h,n,m,s,r,j,s,t,sh,f,m,z,z,str,t,g,k,al-,zh,m,m,z,by,h,g,t,cth,py,l,b,s,g,p,h,sh,ch,shp,h,d,dh,hy,by,k,r,k,k,s,k,al-,h,h,s,l,z,k,al-,sl,k,shr,l,s,b,pl,k,p,h,k,k,k,kn,al-,b,g,br,j,py,shw,shn,s,d,ty,b,g,t,br,y,n,f,spl,y,n,r,v,q,vl,m,shw,k,m,n,h,l,m,th,th,t,g,sm,s,f,r,v,l,k,f,th,gy,l,l,j,shk,h,pn,t,f,l,k,spl,sk,z,r,q,s,l,m,c,t,r,q,b,r,t,m,q,al-,shw,h,g,q,b,f,h,d,p,s,m,k,khm,gr,l,r,th,h,j,kh,h,k,zh,q,zh,sh,r,j,p,sn,p,h,n,f,shw,y,j,khm,h,s,kh,l,sp,s,dh,k,b,v,kl,r,j,n,y,cl,spl,n,j,j,f,s,f,k,s,ibn-,ch,g,j,h,vl,k,l,h,k,spl,zh,ch,t,s,h,s,n,j,ty,f,m,s,s,q,by,n,s,str,j,b,al-,sn,al-,q,ibn-,ch,sh,s,s,r,d,q,q,sh,t,s,w,y,s,mr,mr,q,n,m,h,b,sy,th,sh,zh,t,khm,t,c,cr,s,shw,sh,sp,shr,q,gl,gn,m,h,p,g,sp,b,l,bl,sh,l,sh,j,shw,by,khr,y,khm,l,kl,q,shw,j,c,t,j,al-,k,t,g,g,b,b,b,k,khr,khr,r,z,f,ibn-,m,b,z,k,th,j,l,khr,j,z,h,shn,m,t,g,r,s,l,br,n,c,f,p,q,s,kh,b,s,s,w,p,f,g,z,k,q,hm,s,h,n,l,p,s,h,s,s,gl,h,g,h,h,m,j,c,h,w,d,shw,t,b,j,g,l,gh,l,shp,b,k,g,p,r,j,l,r,s,ibn-,th,z,r,n,zh,l,h,m,b,sp,hm,sh,kh,b,z,c,th,l,n,z,n,k,n,r,j,h,cl,kl,k,k,hm,k,t,s,r,by,k,l,vr,t,r,g,k,p,b,s,f,r,scl,j,r,k,h,kl,shn,q,m,k,b,t,c,h,kh,gh,dh,g,k,q,d,d,g,kh,p,r,r,s,z,q,b,p,l,m,n,l,shw,mr,k,d,z,m,j,h,g,gh,r,ibn-,p,b,s,b,n,f,j,t,al-,r,j,n,f,j,h,y,s,c,g,g,q,th,t,cr,ty,h,scl,b,b,y,k,d,skl,sn,sl,gh,h,pl,h,z,z,kh,z,shm,m,y,sh,sm,n,k,c,j,q,v,p,vl,s,m,al-,h,j,t,n,p,k,sl,j,p,ibn-,shl,r,t,k,l,n,t,k,f,q,f,s,l,hy,y,th,y,zh,s,l,s,kr,j,m,s,l,b,c,shm,str,k,g,h,l,d,f,s,j,g,by,k,gy,j,q,sp,py,spl,kh,t,j,z,v,h,c,ibn-,b,gh,r,p,j,m,l,s,skl,sh,m,tr,q,c,b,h,k,r,t,br,khr,n,j,k,shm,c,z,n,q,spr,sh,l,g,m,b,h,th,khr,t,m,l,h,sh,m,r,k,q,s,g,kr,j,cth,h,t,zh,j,y,n,h,n,br,s,v,kh,tr,l,k,q,s,dh,m,br,p,j,n,l,zh,b,c,n,b,s,my,b,gh,l,w,sy,r,h,k,sy,b,gl,l,h,r,l,spr,l,n,j,j,f,q,th,c,gh,j,j,m,q,c,w,spl,c,shm,h,my,kh,b,al-,c,r,n,s,sh,z,s,m,sh,j,vl,k,sh,r,kh,shw,q,shr,c,q],midConsonants:[ts,f,ny,k,ry,sh,gy,y,m,nn,j,d,b,d,b,tch,h,f,k,py,k,ss,kk,k,g,j,ts,j,sh,t,mm,nz,b,g,hy,k,t,nn,sh,tt,t,sh,ky,ssh,k,p,m,my,sh,ch,t,sh,g,s,z,z,s,b,s,ts,d,y,n,t,sh,n,t,d,t,s,s,d,p,f,b,r,nd,m,r,by,s,r,z,k,y,d,d,s,p,z,z,ts,f,ny,k,ry,sh,gy,y,m,nn,j,d,b,d,b,tch,h,f,k,py,k,ss,kk,k,g,j,ts,j,sh,t,mm,nz,b,g,hy,k,t,nn,sh,tt,t,sh,ky,ssh,k,p,m,my,sh,ch,t,sh,g,s,z,z,s,b,s,ts,d,y,n,t,sh,n,t,d,t,s,s,d,p,f,b,r,nd,m,r,by,s,r,z,k,y,d,d,s,p,z,z,ts,f,ny,k,ry,sh,gy,y,m,nn,j,d,b,d,b,tch,h,f,k,py,k,ss,kk,k,g,j,ts,j,sh,t,mm,nz,b,g,hy,k,t,nn,sh,tt,t,sh,ky,ssh,k,p,m,my,sh,ch,t,sh,g,s,z,z,s,b,s,ts,d,y,n,t,sh,n,t,d,g,rt,shw,gy,kk,zh,kl,l,lth,shw,m,b,mz,pr,s,k,h,kr,b,h,rs,shw,fq,sktr,s,j,l,ps,cs,l,lk,lc,fq,z,zh,gr,h,kh,zh,rch,q,v,scbr,sh,h,j,kh,lk,lg,lpr,p,ss,zh,p,s,s-h,bj,j,s,gl,khm,pt,rsl,n,j,z,s,pr,ntr,k,rcl,rm,kh,br,kk,kk,lth,msh,kk,m,lg,gh,cth,dz,h,sy,s,lq,k,lk,g,h,kk,v,r,nzh,z,f,r,dh,h,r,m,lq,c,dhj,ksh,h,j,nz,q,ns,t,h,dh,rsp,r,s,kk,cth,lb,lgr,l,j,k,k,s,s-h,lc,mp,gh,l,cr,dhj,h,w,m,d,gg,bn,r,z,t,lg,nscr,kh,f,rt,bj,q,kk,sh,h,khr,rch,ngs,sh,f,h,b,ms,rcl,rc,kl,j,lk,nn,t,b,p,sp,vv,c,bj,nst,k,lg,h,cth,rg,nsl,cks,v,f,dtj,h,t,z,hl,c,s,p,ln,ll,p,gl,kr,mj,lk,bz,k,t,h,b,shw,khm,h,ls,q,bj,shm,mm,hl,q,m,lch,g,s,r,mt,s,n,shw,msk,l,lg,c,sh,gr,vv,mt,mj,h,m,j,r,z,dj,f,pl,r,b,sh-h,t,g,dt,h,n,f,jj,dh,sh,mz,mbs,bn,dd,m,kk,h,lsh,kh,tz,m,ltr,bl,s,bz,khm,z,msl,ksh,sh-h,dtj,shp,dt,q,pr,b,bj,rr,l,ksh,r,k,nk,mj,z,kt,d,z,bn,ll,j,h,ll,h,kk,lc,k,l,sk,g,f,ns,mj,rzh,kh,h,q,j,r,jj,bj,kh,mm,rbl,lth,d,dj,cth,n,h,sl,cl,ltr,h,tk,s,nj,zh,tw,dd,mzh,s,g,w,sh,rz,b,rm,z,kk,kh,k,z,l,p,f,s,r,k,ky,q,mt,hy,q,shw-h,b,lkr,n,zh,lf,dh,lk,g,d,r,zh,khm,f,p,g,h,f,dt,j,r,j,k,l,kh,dhj,f,r,h,g,rz,z,h,lj,r,bj,dd,h,gg,rk,h,nzh,n,lf,h,l,h,sp,tw,r,n,g,q,fq,r,lb,h,lsh,h,kk,s,lj,mj,q,pt,rz,m,z,r,g,rs,g,shw,n,gg,p,l,lg,k,ls,bl,h,mbs,sh,s,j,d,v,h,dz,j,lsh,j,mst,jj,ss,k,rbl,cth,l,h,f,s,nt,k,g,l,w,q,l,tr,tw,ks,j,mt,s,rs,dd,shm,c,d,pt,l,l,dd,k,f,r,rs,f,h,shw,q,k,nz,q,p,r,ls,h,d,m,th,m,r,g,kh,c,mz,z,h,z,lk,lth,dd,h,kk,h,q,d,pch,q,shp,l,th,sh,r,z,rzh,msp,dd,dd,r,kk,z,tr,j,s-h,l,tk,hl,z,l,v,j,w,g,r,f,p,f,k,t,b,h,h,nsl,s,shw,rp,k,lf,j,hs,sm,k,z,lg,k,rsc,g,l,r,lc,dj,rtr,r,h,rcl,shw,c,gr,c,pth,ck,t,m,mm,r,k,dd,nst,mj,m,rpl,jj,khr,nsk,lth,zh,c,k,j,gr,rm,ty,mp,gth,r,shr,lf,s,lg,dt,k,k,shw,tz,dz,rkl,mj,dd,lk,t,nj,k,f,rv,zh,n,tz,j,m,k,ln,shk,zh,l,skbr,s,rbl,kh,k,tr,l,r,dtj,h,ksh,rzh,nzh,bz,t,shw,r,k,f,shk,rbl,j,h,mj,g,dh,shw-h,lv,lgr,q,sh,h,rc,khr,mj,ltr,k,m,rc,mj,kk,gl,k,n,dt,cc,rk,kl,shk,shr,lj,ntr,rsp,ck,z,nst,d,tch,mp,l,kh,khm,kk,lpr,nz,t,psh,dt,k,n,j,h,k,s,sctr,f,r,rtr,nsl,rch,khr,l,s,mscr,m,gr,s,rm,j,ntr,lst,c,ps,ntr,f,fq,rk,b,hz,k,gl,lq,k,skbr,mzh,br,l,n,th,h,ct,nskr,sh-h,j,s,nj],closingConsonants:[n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,n,nt,g,n,s,b,gg,jj,nt,s,l,n,khm,p,f,k,k,h,sp,n,l,mbs,b,n,n,k,j,lg,r,h,nch,n,th,n,mb,l,ms,zh,th,rs,jj,r,k,r,q,f,h,z,f,h,nt,s,shw,khm,f,h,gh,ss,k,z,r,jj,cth,jj,p,lc,mbs,rk,k,n,k,sp,mt,t,ls,h,t,r,t,t,k,r,dh,mb,h,pch,th,r,m,v,shw,g,nk,shw,cs,n,th,t,n,r,n,r,r,ksh,f,b,f,ksh,m,rs,rp,kh,l,khr,s,f,kh,z,g,msh,c,v,dh,g,r,c,k,pch,z,r,pt,q,g,r,mbs,sh,t,sk,k,k,v,cs,c,ks,r,g,f,s,r,j,s,s,kh,k,z,t,r,nt,dh,n,v,z,khr,r,h,r,shw,b,d,kh,g,k,k,q,shw,b,r,dt,shw,r,m,d,s,s,f,c,m,b,dh,h,hs,th,j,h,f,nt,j,h,sp,p,rk,h,nk,h,f,j,ss,sh,ls,h,k,t,hs,pch,j,l,ks,r,k,k,h,f,g,z,n,pch,h,sh,q,n,l,lc,nt,q,khr,mt,kh,tch,s,h,sp,b,m,s,m,sh,kh,l,n,n,b,mps,k,h,gh,p,g,sh,d,shw,m,c,n,q,s,ks,shw,rc,f,m,z,s,tch,s,sp,rb,s,r,d,zh,d,h,h,kh,ngs,q,m,j,sh,f,h,l,t,d,shw,g,h,kh,th,zh,m,th,t,s,k,q,j,d,k,j,g,f,tch,z,j,l,b,p,shw,sh,kh,c,k,shw,l,l,j,m,h,b,h,n,mbs,p,p,c,rs,m,zh,k,k,kth,j,j,d,khr,p,mb,b,l,r,h,g,s,r,rk,h,v,k,j,p,c,sc,k,kt,c,q,r,k,sh,z,r,h,k,sp,khr,q,k,p,zh,h,s,n,f,rb,z,c,m,s,z,k,sh,s,p,cth,b,shw,h,lg,c,n,l,h,l,h,ms,b,j,c,k,l,m,khr,c,l,t,f,m,s,shw,m,r,jj,cth,q,ksh,s,h,v,k,k,l,m,r,k,b,r,dt,k,h,g,g,t,n,v,dt,f,r,n,b,zh,mt,sp,r,sh,h,n,d,b,t,b,s,kt,th,rp,h,d,z,shw,j,t,zh,bs,h,k,k,dh,h,b,ns,f,h,q,s,j,kh,p,nk,s,b,p,h,g,t,t,h,j,s,tch,k,cth,t,h,r,sh,h,j,k,v,dh,h,zh,sh,b,k,lk,l,n,s,s,dh,l,l,ng,k,ct,zh,t,h,h,rc,m,z,dh,s,j,kth,j,ls,l,t,n,r,d,khr,mt,kh,cks,l,h,r,mt,q,th,shk,l,s,l,h,z,z,b,n,d,d,b,n,h,rp,dt,h,l,psh,n,lc,z,r,l,kt,k,jj,r,k,ksh,s,f,ngs,lg,h,n,cks,shw,s,j,j,j,h,jj,g,z,t,kh,h,f,q,cs,l,lg,h,zh,q,lsh,f,ksh,s,r,l,j,p,f,lc,mb,lch,k,j,k,z,b,k,zh,gh,t,lc,j,j,t,h,b,rk,d,l,f,l,z,l,b,kh,h,kt,c,l,l,q,r,g,j,sc,kh,j,s,l,g,r,lg,t,kt,k,sk,hs,n,ck,sh,b,h,r,zh,g,b,q,k,z,p,k,p,r,n,k,g,sh,h,b,n,d,q,bs,k,ks,c,s,k,v,k,s,s,j,lc,h,rc,pt,th,s,k,m,f,jj,z,q,n,f,sm,k,w,cth,b,ct,b,p,j,k,b,t,t,r,c,l,ms,q,h,v,k,pch,q,m,j,cks,s,b,cth,mb,l,q,c,g,z,ll,pch,h,ns,j,j,s,sh,d,z,h,j,khm,r,k,m,ks,l,dt,h,b,l,z,h,l,j,q,k,s,nch,zh,t,r,ns,l,s,dt,zh,z,l,l,g,nk,rs,kh,c,khm,f,j,r,r,l,f,n,l,k,p,h,ll,l,m],closingSyllables:[uuq,abiib,ani,adiiq,uuni,abiib,aagh,aagh,abiib,akhmed,ari,ari,akhmen,abiib,akhmed,ani,adiiq,adiiq,uuni,ari,akhmed,iit,ateh,ari,iib,adesh,ani,uuq,iib,adih,adiiq,akhmen,it,amiit,iib,aagh,abiib,uuq,uuni,aagh,it,uuni,uuq,ariid,aagh,uuni,iiz,iiz,iit,ari,adiiq,aqarii,ateh,ari,adesh,uuq,iiz,akhmen,ari,akhmed,it,ari,iit,ari,iiz,aqarii,aqarii,ari,uuni,abiib,adiiq,adih,ateh,ariid,aagh,uuni,ani,akhmen,ariid,aqarii,ani,aiid,aiid,aiid,abiib,ani,adih,iib,ateh,aagh,uuq,uuq,aagh,uuni,ani,adih,ariid,uuni,ani,akhmed,aiid,ari,iit,iiz,aagh,adih,akhmed,iiz,akhmen,adiiq,it,aagh,adih,aagh,it,aqarii,iiz,uuni,ani,aqarii,aagh,uuni,ari,uuni,aiid,adesh,aiid,uuni,aagh,iib,aagh,abiib,aiid,abiib,abiib,ari,aiid,uuq,ari,ariid,amiit,iib,iiz,it,akhmed,uuni,amiit,adesh,ariid,it,aagh,ariid,adesh,iiz,ari,uuq,iiz,abiib,uuni,ateh,amiit,it,ani,iib,it,akhmed,aagh,adesh,aagh,aqarii,ari,amiit,aiid,ani,aiid,aiid,ari,aqarii,ani,aqarii,aagh,abiib,it,iiz,aiid,it,aagh,uuq,ari,aagh,aagh,ari,ari,aiid,ari,aiid,ariid,ari,akhmen,ari,iiz,iiz,aagh,adiiq,iit,ateh,uuni,akhmed,iiz,aagh,ateh,akhmed,iib,iiz,iiz,iib,ari,iib,aqarii,iit,akhmen,ari,uuq,akhmen,ani,uuq,adih,ari,it,ari,aagh,aiid,adih,akhmen,adesh,uuq,aagh,ariid,uuni,iib,ariid,aagh,uuni,ari,ateh,akhmed,ari,uuq,amiit,aagh,iiz,adesh,adiiq,akhmed,akhmed,adesh,adiiq,iiz,ari,ari,ari,amiit,akhmen,aiid,iib,it,iiz,adesh,ari,adesh,iib,aqarii,it,iit,ani,ari,akhmen,aiid,uuni,abiib,ariid,adih,iiz,uuni,adesh,amiit,adesh,aiid,iib,abiib,aiid,ari,amiit,aagh,ariid,aqarii,ari,iit,ani,akhmed,iit,ateh,aagh,akhmen,ani,akhmen,it,iit,adesh,ani,abiib,akhmed,iib,amiit,uuni,abiib,amiit,akhmed,iit,ani,ari,ariid,aagh,ateh,abiib,iit,adih,it,iit,uuq,iib,ariid,abiib,abiib,aagh,iib,ari,adesh,akhmed,adesh,iit,iiz,adesh,adih,adiiq,abiib,aiid,iiz,iib,iib,aagh,adiiq,amiit,iiz,adesh,ari,ari,adih,ari,amiit,aqarii,iiz,aagh,iib,aiid,akhmed,adiiq,aagh,abiib,akhmen,adesh,adih,ariid,ani,akhmed,adiiq,iib,aqarii,ari,adesh,ariid,iit,adesh,it,ariid,ateh,akhmed,amiit,akhmen,aiid,adesh,aagh,amiit,akhmen,akhmed,uuq,aagh,abiib,ari,ateh,iiz,adih,adiiq,adesh,ari,aagh,akhmed,ani,akhmen,uuq,aiid,amiit,akhmen,akhmed,adih,adiiq,akhmed,ani,akhmed,akhmed,aiid,aagh,iit,ari,iit,uuq,iit,iiz,iit,ariid,ari,iit,adesh,akhmed,iiz,aqarii,amiit,ani,ateh,adiiq,akhmen,abiib,ani,aqarii,ariid,aagh,amiit,aqarii,adesh,adih,ariid,adiiq,akhmed,adih,ateh,ani,it,aiid,iib,adih,ani,aagh,adiiq,aagh,akhmen,iib,iiz,ari,aqarii,ari,abiib,adiiq,ani,aagh,ateh,abiib,uuni,amiit,aiid,iit,uuni,aagh,abiib,ari,uuni,ari,iib,ani,it,adesh,abiib,amiit,abiib,akhmen,ateh,ani,uuq,abiib,akhmen,abiib,adiiq,akhmed,aqarii,uuni,uuq,uuni,adiiq,aagh,iib,uuq,ari,aqarii,iib,uuni,adesh,amiit,ari,ariid,ateh,ateh,ani,aiid,it,ari,adiiq,uuni,iit,aagh,aagh,aqarii,ari,ani,adih,iiz,it,uuni,ariid,it,ateh,amiit,ateh,iib,ari,adih,ari,adiiq,iib,iib,uuq,ari,aagh,it,aagh,iit,adesh,uuq,ateh,aagh,aagh,iit,it,amiit,ari,akhmen,adih,aagh,uuq,adiiq,amiit,iit,aagh,amiit,uuq,abiib,amiit,adih,ateh,aiid,iib,iib,aagh,adesh,adih,aagh,adih,iiz,ateh,ani,ani,uuq,adih,uuq,adih,akhmen,aiid,adih,iit,ateh,amiit,aagh,akhmen,ari,ariid,aagh,it,iit,aiid,amiit,iib,ariid,uuq,aagh,iit,amiit,aagh,ari,ariid,ari,akhmen,it,ari,adih,aqarii,iit,iiz,adih,abiib,ariid,akhmen,adih,aqarii,akhmed,akhmed,iit,it,aiid,ari,ari,uuq,ari,ateh,amiit,ari,ateh,akhmed,iiz,ari,aagh,akhmen,aagh,akhmed,akhmen,adiiq,ani,iiz,ari,adiiq,uuni,aiid,aagh,ari,akhmed,aagh,adiiq,aqarii,ariid,amiit,uuq,aqarii,iit,ariid,iiz,aqarii,iit,aqarii,iib,iit,uuq,adesh,aqarii,uuq,uuni,adesh,iit,iiz,it,adih,amiit,uuq,iit,iit,ariid,adiiq,akhmen,uuq,adiiq,aagh,it,ani,adiiq,uuni,aqarii,adesh,ariid,akhmed,aagh,it,amiit,it,it,it,it,amiit,adih,uuni,ariid,it,abiib,akhmen,adiiq,adih,adiiq,adesh,amiit,iib,aqarii,ariid,abiib,ariid,aqarii,abiib,aagh,adesh,it,uuq,ariid,ari,iib,aiid,it,adesh,amiit,akhmen,iib,ariid,amiit,ateh,ateh,aagh,ateh,ariid,iit,iiz,it,aagh,ani,akhmen,adesh,adiiq,adesh,aagh,uuni,abiib,uuq,iib,adiiq,aagh,akhmen,akhmen,iiz,ari,akhmen,ateh,adiiq,adih,amiit,adiiq,it,ari,ariid,ateh,abiib,ari,ateh,aagh,aagh,aiid,adih,ari,ari,aqarii,uuni,amiit,ari,aiid,aagh,iib,abiib,adesh,akhmen,adih,ariid,amiit,aqarii,ateh,iit,akhmed,iit,iiz,aagh,ateh,ani,amiit,ariid,akhmed,iit,aagh,aagh,ariid,ari,aagh,adiiq,ariid,adesh,uuq,ari,ani,aqarii,ateh,aagh,abiib,ari,ateh,iib,akhmed,iib,adesh,abiib,ateh,uuni,ariid,ani,ari,akhmen,uuni,aqarii,it,adiiq,iit,iiz,akhmed,iib,akhmed,ari,aqarii,adesh,iib,akhmen,aiid,adiiq,aqarii,ari,ariid,uuni,ani,ari,aagh,ateh,ateh,uuni,adih,ateh,ari,ani,iib,aagh,uuq,ari,ariid,adih,aqarii,uuq,ateh,akhmed,iiz,aagh,aiid,abiib,uuq,ateh,akhmed,aagh,aagh,ari,ari,aqarii,adih,aagh,adesh,aqarii,adih,ateh,akhmen,uuni,aagh,aagh,ari,adih,akhmen,adesh,aqarii,ani,adiiq,adesh,iiz,adesh,uuq,akhmen,ani,adih,iiz,adiiq,aqarii,aagh,aagh,uuq,uuq,uuq,akhmed,iiz,aqarii,amiit,akhmed,it,iiz,iit,iit,ariid,it,akhmen,adiiq,uuni,aiid,amiit,abiib,aiid,uuq,iib,uuni,ani,uuni,aagh,aagh,adih,adiiq,uuni,aiid,abiib,it,aagh,adih,uuni,aqarii,adih,aiid,adiiq,amiit,iiz,aiid,aiid,uuni,aiid,ariid,iib,adih,aiid,ari,aqarii,it,uuq,adesh,iit,abiib,akhmed,aqarii,ani,ari,amiit,ateh,it,akhmen,abiib,iiz,adiiq,iit,aagh,ari,aiid,ani,ateh],syllableFrequencies:[19.694045299031465,25.326857313503314,13.75743780057484,7.234389899431007,3],totalSyllableFrequency:69.01273031254063,vowelStartFrequency:0.42245718088822853,vowelEndFrequency:0.7087562589321446,syllableEndFrequency:0.08,sanityChecks:[\"[AEIOUaeiou]{3}!\",\"(\\\\p{L})\\\\1\\\\1!\",\"[Ii][iyq]!\",\"[Yy]([aiu])\\\\1!\",\"[Rr][uy]+[rh]!\",\"[Qq]u[yu]!\",\"[^oaei]uch!\",\"[Hh][tcszi]?h!\",\"[Tt]t[^aeiouy]{2}!\",\"[Yy]h([^aeiouy]|$)!\",\"([xqy])\\\\1$!\",\"[qi]y$!\",\"[szSZrlRL]+?[^aeiouytdfgkcpbmnslrv][rlsz]!\",\"[UIuiYy][wy]!\",\"^[UIui]e!\",\"^([^aeioyl])\\\\1#\"],summary:0#1384785347551869630@4.0~12@5.0~8@3.0,name:Arabic Romanized/Nameless Language/Japanese Romanized}"
            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme"), w2;
            world.generate(15, true);
            GreasedRegion grease = new GreasedRegion(new DiverRNG(75L), 75, 75), g2;
            store.put("rng", srng);
            store.put("language", randomLanguage);
            store.put("world", world);
            store.put("grease", grease);

            store.store("Test");
            System.out.println(store.show());

            System.out.println("Stored preference bytes: " + store.preferencesSize());
            r2 = store.get("Test", "rng", StatefulRNG.class);
            lang2 = store.get("Test", "language", FakeLanguageGen.class);
            w2 = store.get("Test", "world", SpillWorldMap.class);
            g2 = store.get("Test", "grease", GreasedRegion.class);
            long seed1 = srng.getState(), seed2 = r2.getState();
            System.out.println("StatefulRNG states equal: " + (seed1 == seed2));
            System.out.println("FakeLanguageGen values equal: " + randomLanguage.equals(lang2));
            System.out.println("FakeLanguageGen outputs equal: " + randomLanguage.sentence(srng, 5, 10).equals(lang2.sentence(r2, 5, 10)));
            System.out.println("SpillWorldMap.politicalMap values equal: " + Arrays.deepEquals(world.politicalMap, w2.politicalMap));
            System.out.println("SpillWorldMap.atlas values equal: " + world.atlas.equals(w2.atlas));
            System.out.println("GreasedRegion values equal: " + grease.equals(g2));

            store.preferences.clear();
            store.preferences.flush();
            Gdx.app.exit();
        }
        else {

            SquidStorage noCompression = new SquidStorage("StorageTest"), yesCompression = new SquidStorage("StorageCompressed");
            noCompression.compress = false;
            yesCompression.compress = true;
            System.out.println(noCompression.preferences.get().values());
            System.out.println(yesCompression.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!");

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, FakeLanguageGen.ARABIC_ROMANIZED, 5, FakeLanguageGen.JAPANESE_ROMANIZED, 3);

            EnumMap<Direction, String> empty = new EnumMap<>(Direction.class);
            EnumOrderedMap<Direction, String> empty2 = new EnumOrderedMap<>();
            TestClass em = new TestClass();
            em.initialize();
            noCompression.json.setElementType(TestClass.class, "em", String.class);
            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme");
            world.generate(15, true);
            GreasedRegion grease = new GreasedRegion(75, 75);
            grease.insertRectangle(10, 10, 55, 55).removeRectangle(20, 20, 45, 45);
            String text = randomLanguage.sentence(srng.copy(), 5, 8);
            ProbabilityTable<String> table = new ProbabilityTable<>("I heard you like JSON...");
            table.add("well", 1).add("this", 2).add("ain't", 3).add("real", 4).add("JSON!", 5);
            //String text = table.random();
            Coord point = Coord.get(42, 23);

            noCompression.put("rng", srng);
            noCompression.put("language", randomLanguage);
            noCompression.put("generated", text);
            noCompression.put("world", world);
            noCompression.put("grease", grease);
            noCompression.put("table", table);
            noCompression.put("drawn", text);
            noCompression.put("enum_map", em);
            noCompression.put("empty_enum_map", empty);
            noCompression.put("empty_eom", empty2);
            noCompression.put("coord", point);
            
            yesCompression.put("rng", srng);
            yesCompression.put("language", randomLanguage);
            yesCompression.put("generated", text);
            yesCompression.put("world", world);
            yesCompression.put("grease", grease);
            yesCompression.put("table", table);
            yesCompression.put("drawn", text);
            yesCompression.put("enum_map", em);
            yesCompression.put("empty_enum_map", empty);
            yesCompression.put("empty_eom", empty2);
            yesCompression.put("coord", point);

            System.out.println(text);

            String shown = noCompression.show();
            System.out.println(shown);
            System.out.println("Uncompressed preference bytes: " + shown.length() * 2);
            shown = yesCompression.show();
            System.out.println();
            System.out.println(shown);
            System.out.println("Compressed preference bytes: " + shown.length() * 2);
            noCompression.preferences.clear();
            noCompression.preferences.flush();
            yesCompression.store("Compressed");

            System.out.println(yesCompression.get("Compressed", "language", FakeLanguageGen.class).sentence(srng.copy(), 5, 8));
            System.out.println(yesCompression.get("Compressed", "drawn", String.class));
            System.out.println(em);
            System.out.println(yesCompression.get("Compressed", "enum_map", TestClass.class));

            System.out.println(table.random());
            System.out.println(yesCompression.get("Compressed", "table", ProbabilityTable.class).random());

            //note, these are different because EnumMap needs the enum's Class to be constructed, and an empty EnumMap
            //can't have any keys' Class queried (no keys are present). EnumMap has a field that stores the Class as a
            //final field, but it's private so we can't safely use it.
            System.out.println(empty);
            System.out.println(yesCompression.get("Compressed", "empty_enum_map", EnumMap.class));
            System.out.println(empty2);
            System.out.println(yesCompression.get("Compressed", "empty_eom", EnumOrderedMap.class));
            System.out.println(point);
            System.out.println(yesCompression.get("Compressed", "coord", Coord.class));
            System.out.println(yesCompression.get("Compressed", "grease", GreasedRegion.class).andNot(grease).isEmpty());
            yesCompression.preferences.clear();
            yesCompression.preferences.flush();
            Gdx.app.exit();
        }
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new SquidStorageTest(), config);
    }
}
