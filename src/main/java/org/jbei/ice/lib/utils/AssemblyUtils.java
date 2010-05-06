package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Part.AssemblyStandard;

public interface AssemblyUtils {

    public AssemblyStandard determineAssemblyStandard(String partSequenceString)
            throws UtilityException;

    public SequenceFeatureCollection determineAssemblyFeatures(Sequence partSequence)
            throws UtilityException;

    public Sequence join(Part part1, Part part2) throws UtilityException;
}