/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.dem.dataio.srtm1_esrigrid;

import org.esa.snap.framework.datamodel.GeoPos;
import org.esa.snap.framework.dataop.dem.AbstractElevationModelDescriptor;
import org.esa.snap.framework.dataop.dem.ElevationModel;
import org.esa.snap.framework.dataop.maptransf.Datum;
import org.esa.snap.framework.dataop.resamp.Resampling;

import java.net.URL;

public class SRTM1GridElevationModelDescriptor extends AbstractElevationModelDescriptor {

    public static final String NAME = "SRTM 1Sec Grid";
    public static final String DB_FILE_SUFFIX = ".adf";
    public static final int NUM_X_TILES = 360;
    public static final int NUM_Y_TILES = 120;
    public static final int DEGREE_RES = 1;
    public static final int PIXEL_RES = 3600;
    public static final int NO_DATA_VALUE = -32768;

    public static final GeoPos RASTER_ORIGIN = new GeoPos(60, 180);
    public static final int RASTER_WIDTH = NUM_X_TILES * PIXEL_RES;
    public static final int RASTER_HEIGHT = NUM_Y_TILES * PIXEL_RES;

    public SRTM1GridElevationModelDescriptor() {
    }

    public String getName() {
        return NAME;
    }

    public int getNumXTiles() {
        return NUM_X_TILES;
    }

    public int getNumYTiles() {
        return NUM_Y_TILES;
    }

    public float getNoDataValue() {
        return NO_DATA_VALUE;
    }

    public int getRasterWidth() {
        return RASTER_WIDTH;
    }

    public int getRasterHeight() {
        return RASTER_HEIGHT;
    }

    public GeoPos getRasterOrigin() {
        return RASTER_ORIGIN;
    }

    public int getDegreeRes() {
        return DEGREE_RES;
    }

    public int getPixelRes() {
        return PIXEL_RES;
    }

    @Override
    public boolean canBeDownloaded() {
        return false;
    }

    public URL getDemArchiveUrl() {
        return null;
    }

    public ElevationModel createDem(final Resampling resamplingMethod) {
        try {
            return new SRTM1GridElevationModel(this, resamplingMethod);
        } catch (Exception e) {
            return null;
        }
    }

    public String createTileFilename(final int minLat, final int minLon) {
        final StringBuilder name = new StringBuilder(12);

        name.append(minLon < 0 ? "w" : "e");
        String lonString = String.valueOf(Math.abs(minLon));
        while (lonString.length() < 2) {
            lonString = '0' + lonString;
        }
        name.append(lonString);

        name.append('_');

        name.append(minLat < 0 ? "s" : "n");
        String latString = String.valueOf(Math.abs(minLat));
        while (latString.length() < 2) {
            latString = '0' + latString;
        }
        name.append(latString);

        return name.toString();
    }

}
