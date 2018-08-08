package iox.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.util.MersenneTwisterFast;

public class MTF {

	private static final Logger log = LoggerFactory.getLogger(MTF.class);

	private static MersenneTwisterFast mtf;

	public static final int DEFAULT_SAMPLE_SIZE = 2500;

	public static Integer[] generateRands(final int range) {
		return generateRands(range, DEFAULT_SAMPLE_SIZE, false);
	}

	public static Integer[] generateRands(final int range, final int sampleSize) {
		return generateRands(range, sampleSize, false);
	}

	public static Integer[] generateRands(final int range,
			final boolean sortFirst) {
		return generateRands(range, DEFAULT_SAMPLE_SIZE, sortFirst);
	}

	public static Integer[] generateRands(final int range,
			final int sampleSize, final boolean sortFirst) {
		Set<Integer> set = new HashSet<Integer>(1);
		while (set.size() < sampleSize) {
			Integer I = MTF.getNextAsInteger(range);
			set.add(I);
		}
		Integer[] II = set.toArray(new Integer[set.size()]);
		if (sortFirst) {
			Arrays.sort(II);
		}
		log.trace(Arrays.toString(II));
		return II;
	}

	public static boolean getNextAsBoolean() {
		return getMtf().nextBoolean();
	}

	public static boolean getNextAsBoolean(double probability) {
		return getMtf().nextBoolean(probability);
	}

	public static boolean getNextAsBoolean(float probability) {
		return getMtf().nextBoolean(probability);
	}

	public static double getNextAsDouble() {
		return getMtf().nextDouble();
	}

	public static int getNextAsInteger() {
		return getMtf().nextInt();
	}

	public static int getNextAsInteger(int range) {
		return getMtf().nextInt(range);
	}

	private static MersenneTwisterFast getMtf() {
		if (mtf == null) {
			mtf = new MersenneTwisterFast();
		}
		return mtf;
	}
}