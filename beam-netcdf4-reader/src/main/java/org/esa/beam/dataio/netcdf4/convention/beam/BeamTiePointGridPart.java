/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.netcdf4.convention.beam;

import org.esa.beam.dataio.netcdf4.Nc4ReaderParameters;
import org.esa.beam.dataio.netcdf4.convention.HeaderDataJob;
import org.esa.beam.dataio.netcdf4.convention.HeaderDataWriter;
import org.esa.beam.dataio.netcdf4.convention.ModelPart;
import org.esa.beam.dataio.netcdf4.convention.cf.CfBandPart;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class BeamTiePointGridPart implements ModelPart {

    public final String OFFSET_X = "offset_x";
    public final String OFFSET_Y = "offset_y";
    public final String SUBSAMPLING_X = "subsampling_x";
    public final String SUBSAMPLING_Y = "subsampling_y";

    @Override
    public void read(Product p, Nc4ReaderParameters rp) throws IOException {
        final List<Variable> variables = rp.getGlobalVariables();
        for (Variable variable : variables) {
            final List<Dimension> dimensions = variable.getDimensions();
            if (dimensions.size() != 2) {
                continue;
            }
            final Dimension dimensionY = dimensions.get(0);
            final Dimension dimensionX = dimensions.get(1);
            if (dimensionY.getLength() != p.getSceneRasterHeight()
                || dimensionX.getLength() != p.getSceneRasterWidth()) {
                //maybe this is a tie point grid
                final String name = variable.getName();
                final Attribute offsetX = variable.findAttributeIgnoreCase(OFFSET_X);
                final Attribute offsetY = variable.findAttributeIgnoreCase(OFFSET_Y);
                final Attribute subSamplingX = variable.findAttributeIgnoreCase(SUBSAMPLING_X);
                final Attribute subSamplingY = variable.findAttributeIgnoreCase(SUBSAMPLING_Y);
                if (offsetX != null && offsetY != null &&
                        subSamplingX != null && subSamplingY != null) {
                    final Array array = variable.read();
                    final float[] data = new float[(int) array.getSize()];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = array.getFloat(i);
                    }
                    final boolean containsAngles = "lon".equalsIgnoreCase(name) || "lat".equalsIgnoreCase(name);
                    final TiePointGrid grid = new TiePointGrid(name,
                                                           dimensionX.getLength(),
                                                           dimensionY.getLength(),
                                                           offsetX.getNumericValue().floatValue(),
                                                           offsetY.getNumericValue().floatValue(),
                                                           subSamplingX.getNumericValue().floatValue(),
                                                           subSamplingY.getNumericValue().floatValue(),
                                                           data,
                                                           containsAngles);
                    CfBandPart.applyAttributes(grid, variable);
                    p.addTiePointGrid(grid);
                }
            }
        }
    }

    @Override
    public void write(Product p, final NetcdfFileWriteable ncFile, HeaderDataWriter hdw) throws IOException {
        final TiePointGrid[] tiePointGrids = p.getTiePointGrids();
        final HashMap<String, Dimension[]> dimMap = new HashMap<String, Dimension[]>();
        for (TiePointGrid grid : tiePointGrids) {
            final String key = "" + grid.getRasterHeight() + " " + grid.getRasterWidth();
            if (!dimMap.containsKey(key)) {
                final int size = dimMap.size();
                final String suffix = size > 0 ? "" + (size + 1) : "";
                final Dimension[] dimensions = {
                        new Dimension("tp_y" + suffix, grid.getRasterHeight()),
                        new Dimension("tp_x" + suffix, grid.getRasterWidth())
                };
                dimMap.put(key, dimensions);
                ncFile.addDimension(null, dimensions[0]);
                ncFile.addDimension(null, dimensions[1]);
            }
            final Variable variable = ncFile.addVariable(grid.getName(), DataType.FLOAT, dimMap.get(key));
            variable.addAttribute(new Attribute(OFFSET_X, grid.getOffsetX()));
            variable.addAttribute(new Attribute(OFFSET_Y, grid.getOffsetY()));
            variable.addAttribute(new Attribute(SUBSAMPLING_X, grid.getSubSamplingX()));
            variable.addAttribute(new Attribute(SUBSAMPLING_Y, grid.getSubSamplingY()));
        }
        hdw.registerHeaderDataJob(new HeaderDataJob() {
            @Override
            public void go() throws IOException {
                for (TiePointGrid tiePointGrid : tiePointGrids) {
                    try {
                        final int y = tiePointGrid.getRasterHeight();
                        final int x = tiePointGrid.getRasterWidth();
                        final int[] shape = new int[]{y, x};
                        final Array values = Array.factory(DataType.FLOAT, shape, tiePointGrid.getDataElems());
                        ncFile.write(tiePointGrid.getName(), values);
                    } catch (InvalidRangeException e) {
                        throw new ProductIOException("TiePointData not in the expected range");
                    }
                }
            }
        });
    }
}
