/* blacken - a library for Roguelike games
 * Copyright © 2011 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.googlecode.blacken.navierstokes;

import com.googlecode.blacken.grid.Grid;

/**
 * This code mostly comes from:
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pub.html
 * Jos Stam, "Real-Time Fluid Dynamics for Games". 
 *           Proceedings of the Game Developer Conference, March 2003
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
 * 
 * @author yam655
 *
 */
public class NavierStokes {
    float dt = 10F;
    float diff = 0.5F;
    float visc = 0.5f;
    int layer = 0;
    
    boolean uAlt = false;
    boolean vAlt = false;
    boolean dAlt = false;
    private Grid<? extends SupportsNavierStokes> grid;
    static final int U_IDX = 0;
    static final int V_IDX = 1;
    static final int DENSITY_IDX = 2;
    static final int ALTERNATE_OFFSET = 3;

    private void addSource(int idx) {
        /*
        void add_source ( int N, float[] x, float[] s, float dt) {
            int i, size=(N+2)*(N+2);
            for ( i=0 ; i<size ; i++ ) x[i] += dt*s[i];
        }
        */
        for (int y = grid.getY(); y < grid.getHeight() + grid.getY(); y++) {
            for (int x = grid.getX(); x < grid.getWidth() + grid.getX(); x++) {
                setNS(x, y, idx, dt * getNS(x, y, idx + ALTERNATE_OFFSET));
            }
        }
    }
    
    private void diffuse(int b, int idx) {
        /*
        void diffuse ( int N, int b, float[] x, float[] x0, float diff, float dt) {
            int i, j, k;
            float a=dt*diff*N*N;
            for ( k=0 ; k<20 ; k++ ) {
                for ( i=1 ; i<=N ; i++ ) {
                    for ( j=1 ; j<=N ; j++ ) {
                        x[IX(i,j)] = (x0[IX(i,j)] + a*(x[IX(i-1,j)]+x[IX(i+1,j)]+
                                      x[IX(i,j-1)]+x[IX(i,j+1)]))/(1+4*a);
                    }
                }
                set_bnd ( N, b, x );
            }
        }
        */
        int N = grid.getHeight() * grid.getWidth();
        float a = dt * diff * N * N;
        for (int k=0; k < 20; k++) {
            for (int i = 1; i < grid.getWidth()-1; i++) {
                for (int j = 1; j < grid.getHeight()-1; j++) {
                    float sum = getNS(i-1, j, idx) + getNS(i+1, j, idx) +
                                getNS(i, j-1, idx) + getNS(i, j+1, idx);
                    setNS(i, j, idx, (getNS(i, j, idx + ALTERNATE_OFFSET) 
                                      + a * sum) / 1 + 4 * a);
                }
            }
            setBoundary(b, idx);
        }
    }
    private float getNS(int x, int y, int idx) {
        switch(idx) {
        case U_IDX + ALTERNATE_OFFSET:
            if (uAlt) idx -= ALTERNATE_OFFSET;
            break;
        case U_IDX:
            if (uAlt) idx += ALTERNATE_OFFSET;
            break;
        case V_IDX + ALTERNATE_OFFSET:
            if (vAlt) idx -= ALTERNATE_OFFSET;
            break;
        case V_IDX:
            if (vAlt) idx += ALTERNATE_OFFSET;
            break;
        case DENSITY_IDX + ALTERNATE_OFFSET:
            if (dAlt) idx -= ALTERNATE_OFFSET;
            break;
        case DENSITY_IDX:
            if (dAlt) idx += ALTERNATE_OFFSET;
            break;
        }
        return grid.get(y, x).getNavierStokes(idx, this.layer);
    }

    private void swap(int idx) {
        switch(idx) {
        case U_IDX:
            this.uAlt = !this.uAlt;
            break;
        case V_IDX:
            this.vAlt = !this.vAlt;
            break;
        case DENSITY_IDX:
            this.dAlt = !this.dAlt;
            break;
        }
    }
    private void setNS(int x, int y, int idx, float value) {
        switch(idx) {
        case U_IDX+ALTERNATE_OFFSET:
            if (this.uAlt) idx -= ALTERNATE_OFFSET;
            break;
        case U_IDX:
            if (this.uAlt) idx += ALTERNATE_OFFSET;
            break;
        case V_IDX+ALTERNATE_OFFSET:
            if (this.vAlt) idx -= ALTERNATE_OFFSET;
            break;
        case V_IDX:
            if (this.vAlt) idx += ALTERNATE_OFFSET;
            break;
        case DENSITY_IDX+ALTERNATE_OFFSET:
            if (this.dAlt) idx -= ALTERNATE_OFFSET;
            break;
        case DENSITY_IDX:
            if (this.dAlt) idx += ALTERNATE_OFFSET;
            break;
        }
        grid.get(y, x).setNavierStokes(idx, this.layer, value);
    }
    
    private void advect(int b, int d, int d0, int u, int v) {
        /*
        void advect (int N, int b, float[] d, float[] d0, float[] u, float[] v, float dt) {
            int i, j, i0, j0, i1, j1;
            float x, y, s0, t0, s1, t1, dt0;
            dt0 = dt*N;
            for ( i=1 ; i<=N ; i++ ) {
                for ( j=1 ; j<=N ; j++ ) {
                    x = i-dt0*u[IX(i,j)]; 
                    y = j-dt0*v[IX(i,j)];
                    if (x<0.5F) x=0.5F; 
                    if (x>N+0.5F) x=N+ 0.5F; 
                    i0=(int)x; i1=i0+1;
                    if (y<0.5F) y=0.5F; 
                    if (y>N+0.5F) y=N+ 0.5F; 
                    j0=(int)y; 
                    j1=j0+1;
                    s1 = x-i0; s0 = 1-s1; 
                    t1 = y-j0; t0 = 1-t1;
                    d[IX(i,j)] = s0*(t0*d0[IX(i0,j0)]+t1*d0[IX(i0,j1)])+
                    s1*(t0*d0[IX(i1,j0)]+t1*d0[IX(i1,j1)]);
                }
            }
            set_bnd ( N, b, d );
        }
        */
        int i, j, i0, j0, i1, j1;
        float x, y, s0, t0, s1, t1, dt0;
        int maxDim = grid.getHeight();
        if (grid.getWidth() > maxDim) {
            maxDim = grid.getWidth();
        }
        dt0 = dt*maxDim;
        for ( i=1 ; i<=maxDim ; i++ ) {
            for ( j=1 ; j<=maxDim ; j++ ) {
                x = i - dt0 * getNS(i,j, u); 
                y = j - dt0 * getNS(i,j, v);
                if (x < 0.5F) x=0.5F; 
                if (x > maxDim + 0.5F) x = maxDim + 0.5F; 
                i0 = (int)x; 
                i1 = i0+1;
                if (y < 0.5F) y=0.5F; 
                if (y > maxDim + 0.5F) y = maxDim + 0.5F; 
                j0 = (int)y; 
                j1 = j0+1;
                s1 = x - i0; s0 = 1 - s1; 
                t1 = y - j0; t0 = 1 - t1;
                float partial = 
                    (t0 * getNS(i0,j0, d0) +
                     t1 * getNS(i0,j1, d0) +
                     s1 * (t0 * getNS(i1,j0, d0) + 
                           t1 * getNS(i1,j1, d0)));
                setNS(i, j, d, s0 * partial);
            }
        }
        setBoundary(b, d);
    }

    protected void densityStep() {
        /*
        void dens_step ( int N, float[] x, float[] x0, float[] u, float[] v, 
                         float diff, float dt ) {
            float[] tmp;
            add_source(N, x, x0, dt);
            tmp = x0; x0 = x; x = tmp;
            diffuse ( N, 0, x, x0, diff, dt );
            tmp = x0; x0 = x; x = tmp;
            advect ( N, 0, x, x0, u, v, dt );
        }
        */
        addSource(DENSITY_IDX);
        swap(DENSITY_IDX);
        diffuse(0, DENSITY_IDX);
        swap(DENSITY_IDX);
        advect(0, DENSITY_IDX, DENSITY_IDX|ALTERNATE_OFFSET,
                  U_IDX, V_IDX);
    }


    private void velocityStep() {
        /*
        void vel_step (int N, float[] u, float[] v, float[] u0, float[] v0,
                        float visc, float dt) {
            add_source ( N, u, u0, dt ); 
            add_source ( N, v, v0, dt );
            SWAP(u, u0);
            diffuse ( N, 1, u, u0, visc, dt );
            SWAP(v, v0);
            diffuse ( N, 2, v, v0, visc, dt );
            project ( N, u, v, u0, v0 );
            SWAP(u, u0); SWAP(v, v0);
            advect ( N, 1, u, u0, u0, v0, dt ); 
            advect ( N, 2, v, v0, u0, v0, dt );
            project ( N, u, v, u0, v0 );
        }
        */
        addSource(U_IDX);
        addSource(V_IDX);
        swap(U_IDX);
        diffuse(1, U_IDX);
        swap(V_IDX);
        diffuse(2, V_IDX);
        project();
        swap(U_IDX);
        swap(V_IDX);
        advect(1, U_IDX, U_IDX|ALTERNATE_OFFSET,
               U_IDX+ALTERNATE_OFFSET, V_IDX+ALTERNATE_OFFSET);
        advect(1, V_IDX, V_IDX|ALTERNATE_OFFSET,
               U_IDX+ALTERNATE_OFFSET, V_IDX+ALTERNATE_OFFSET);
        project();
    }
    

    private void project() {
        /*
        void project ( int N, float[] u, float[] v, float[] p, float[] div ) {
            int i, j, k;
            float h;
            h = 1.0f/N;
            for ( i=1 ; i<=N ; i++ ) {
                for ( j=1 ; j<=N ; j++ ) {
                    div[IX(i,j)] = -0.5F*h*(u[IX(i+1,j)]-u[IX(i-1,j)]+
                    v[IX(i,j+1)]-v[IX(i,j-1)]);
                    p[IX(i,j)] = 0;
                }
            }
            set_bnd ( N, 0, div ); set_bnd ( N, 0, p );
            for ( k=0 ; k<20 ; k++ ) {
                for ( i=1 ; i<=N ; i++ ) {
                    for ( j=1 ; j<=N ; j++ ) {
                        p[IX(i,j)] = (div[IX(i,j)]+p[IX(i-1,j)]+p[IX(i+1,j)]+
                        p[IX(i,j-1)]+p[IX(i,j+1)])/4;
                    }
                }
                set_bnd ( N, 0, p );
            }
            for ( i=1 ; i<=N ; i++ ) {
                for ( j=1 ; j<=N ; j++ ) {
                    u[IX(i,j)] -= 0.5*(p[IX(i+1,j)]-p[IX(i-1,j)])/h;
                    v[IX(i,j)] -= 0.5*(p[IX(i,j+1)]-p[IX(i,j-1)])/h;
                }
            }
            set_bnd ( N, 1, u ); 
            set_bnd ( N, 2, v );
        }
        */
        int p_idx = U_IDX+ALTERNATE_OFFSET;
        int div_idx = V_IDX+ALTERNATE_OFFSET;
        int i, j, k;
        float h;
        int maxDim = grid.getHeight();
        if (grid.getWidth() > maxDim) {
            maxDim = grid.getWidth();
        }
        h = 1.0f / maxDim;
        for ( i=1 ; i < grid.getWidth()-1; i++ ) {
            for ( j=1 ; j < grid.getHeight()-1; j++ ) {
                setNS(i, j, div_idx, -0.5F * h * (getNS(i+1,j, U_IDX)) - 
                      getNS(i-1,j, U_IDX) + getNS(i,j+1, V_IDX) -
                      getNS(i,j-1, V_IDX));
                setNS(i, j, p_idx, 0);
            }
        }
        setBoundary(0, div_idx);
        setBoundary(0, p_idx);
        for (k = 0; k < 20; k++) {
            for (i = 1; i <= grid.getWidth()-1; i++) {
                for (j = 1; j <= grid.getHeight()-1; j++) {
                    setNS(i,j, p_idx, (getNS(i, j, div_idx) + 
                                       getNS(i-1, j, p_idx) +
                                       getNS(i+1, j, p_idx) +
                                       getNS(i, j-1, p_idx) +
                                       getNS(i, j+1, p_idx))/4);
                }
            }
            setBoundary(0, p_idx);
        }
        for (i=1; i <= grid.getWidth() - 1; i++) {
            for (j=1; j <= grid.getHeight() - 1; j++) {
                setNS(i, j, U_IDX, getNS(i, j, U_IDX) - 
                      0.5F * (getNS(i+1,j,p_idx) - getNS(i-1,j,p_idx))/h);
                setNS(i, j, V_IDX, getNS(i, j, V_IDX) - 
                      0.5F * (getNS(i,j+1,p_idx) - getNS(i,j-1,p_idx))/h);
            }
        }
        setBoundary(1, U_IDX);
        setBoundary(2, V_IDX);
    }

    /**
     * @param b
     * @param i
     */
    private void setBoundary(int b, int idx) {
        /*
        void set_bnd ( int N, int b, float[] x ) {
            int i;
            for ( i=1 ; i<=N ; i++ ) {
                x[IX(0 ,i)] = b==1 ? -x[IX(1,i)] : x[IX(1,i)];
                x[IX(N+1,i)] = b==1 ? -x[IX(N,i)] : x[IX(N,i)];
                x[IX(i,0 )] = b==2 ? -x[IX(i,1)] : x[IX(i,1)];
                x[IX(i,N+1)] = b==2 ? -x[IX(i,N)] : x[IX(i,N)];
            }
            x[IX(0 ,0 )] = 0.5F*(x[IX(1,0 )]+x[IX(0 ,1)]);
            x[IX(0 ,N+1)] = 0.5F*(x[IX(1,N+1)]+x[IX(0 ,N )]);
            x[IX(N+1,0 )] = 0.5F*(x[IX(N,0 )]+x[IX(N+1,1)]);
            x[IX(N+1,N+1)] = 0.5F*(x[IX(N,N+1)]+x[IX(N+1,N )]);
        }
        */
        for (int y = 0; y < grid.getHeight(); y++) { 
            if (y == 0) {
                for (int x = 1; x < grid.getWidth()-1; x++) {
                    float val = grid.get(y+1, x).getNavierStokes(idx, layer); 
                    grid.get(y, x).setNavierStokes(idx, layer, b==2 ? -val : val);
                }
            } else if (y == grid.getHeight()-1) {
                for (int x = 1; x <= grid.getWidth()-1; x++) {
                    float val = grid.get(y-1, x).getNavierStokes(idx, layer); 
                    grid.get(y, x).setNavierStokes(idx, layer, b==2 ? -val : val);
                }
            } else {
                float val = grid.get(y, 1).getNavierStokes(idx, layer); 
                grid.get(y, 0).setNavierStokes(idx, layer, b==1 ? -val : val);
                val = grid.get(y, grid.getWidth()-2).getNavierStokes(idx, layer); 
                grid.get(y, grid.getWidth()-1).setNavierStokes(idx, layer, b==1 ? -val : val);
            }
        }
        float val = grid.get(0, 1).getNavierStokes(idx, layer) +
                    grid.get(1, 0).getNavierStokes(idx, layer);
        grid.get(0, 0).setNavierStokes(idx, layer, 0.5F * val);
        
        val = grid.get(0, grid.getWidth()-2).getNavierStokes(idx, layer) +
              grid.get(1, grid.getWidth()-1).getNavierStokes(idx, layer);
        grid.get(0, grid.getWidth()-1).setNavierStokes(idx, layer, 0.5F * val);
        
        val = grid.get(grid.getHeight()-1, 1).getNavierStokes(idx, layer) +
              grid.get(grid.getHeight()-2, 0).getNavierStokes(idx, layer);
        grid.get(grid.getHeight()-1, 0).setNavierStokes(idx, layer, 0.5F * val);
        
        val = grid.get(grid.getHeight()-1, grid.getWidth()-2).getNavierStokes(idx, layer) +
              grid.get(grid.getHeight()-2, grid.getWidth()-1).getNavierStokes(idx, layer);
        grid.get(grid.getHeight()-1, grid.getWidth()-1).setNavierStokes(idx, layer, 0.5F * val);
    }

    /**
     * simulate Navier-Stokes on a Grid
     * @param grid the grid to run on
     * @param layer layer within the grid
     * @param visc the viscosity
     * @param diff the diffusion
     * @param dt the time-step
     */
    public void simulate(Grid<? extends SupportsNavierStokes> grid, 
                    int layer, float visc, float diff, int dt) {
        /*
        void simulateOld() {
            boolean simulating = true;
            while ( simulating ) {
                //get_from_UI ( dens_prev, u_prev, v_prev );
                vel_step ( N, u, v, u_prev, v_prev, visc, dt );
                dens_step ( N, dens, dens_prev, u, v, diff, dt );
                //draw_dens ( N, dens );
            }
        }
        */
        this.visc = visc;
        this.dt = dt;
        this.diff = diff;
        this.layer = layer;
        this.grid = grid;
        velocityStep();
        densityStep();
    }
}
