
/**
 * 
 * Cydia Stat Checker
 * By PoomSmart
 * 

 *
 * Note about this program
 * 
 * 1. Packages which version format is one of a.b-c, a.b.c and a.b.c-d are supported
 * 2. normalizedName(String) method may be used to fix the issue about same tweak but different names
 * 3. You may plot downloads data from multiple tweaks
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Checker {

	public static Map<String, Map<String, Integer>> tweaks = new HashMap<String, Map<String, Integer>>();
	public static Map<String, Map<String, Integer>> flipswitches = new HashMap<String, Map<String, Integer>>();
	public static Map<String, Integer> submits = new HashMap<String, Integer>();
	public static List<String> outdatedTweaks = new Vector<String>();
	public static Pattern versionPattern1 = Pattern.compile("(\\d+)\\.(\\d+)\\-(\\d+)");
	public static Pattern versionPattern2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\-(\\d+)");
	public static Pattern versionPattern3 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
	public static Pattern versionPattern4 = Pattern.compile("(\\d+)\\.(\\d+)");
	public static Matcher matcher;
	public static boolean includeOutdated = true;
	public static boolean fetchFromLink = true;

	public static String developer = "PoomSmart";
	public static String filename = "stat-" + developer + ".txt";
	public static String URL = "http://apt.thebigboss.org/stats.php?dev=poomst";

	public static int totalDownloads = 0;

	public static int toInt(String str) {
		return Integer.parseInt(str);
	}

	public static String normalizedVersion(String version) {
		if (version.equals("1.0"))
			return "1.0-0";
		if (version.equals("0.0.1"))
			return "0.0.1-0";
		if (version.equals("1.0-9-1"))
			return "1.0-9";
		return version;
	}

	public static int versionScore(String scoreStr) {
		matcher = versionPattern4.matcher(scoreStr);
		if (matcher.matches())
			return toInt(matcher.group(1)) * 10000 + toInt(matcher.group(2)) * 100;
		matcher = versionPattern1.matcher(scoreStr);
		if (matcher.matches())
			return toInt(matcher.group(1)) * 10000 + toInt(matcher.group(2)) * 100 + toInt(matcher.group(3));
		matcher = versionPattern2.matcher(scoreStr);
		if (matcher.matches())
			return toInt(matcher.group(1)) * 1000000 + toInt(matcher.group(2)) * 10000 + toInt(matcher.group(3)) * 100
					+ toInt(matcher.group(4));
		matcher = versionPattern3.matcher(scoreStr);
		if (matcher.matches())
			return toInt(matcher.group(1)) * 10000 + toInt(matcher.group(2)) * 100 + toInt(matcher.group(3));
		return 0;
	}

	public static String normalizedName(String name) {
		if (outdatedTweaks.contains(name) && !includeOutdated)
			return null;
		if (name.equals("Still Capture Enabler"))
			return "Still Capture Enabler 2";
		if (name.equals("Animted Weather Enabler"))
			return "Animated Weather Enabler";
		if (name.equals("CamVolNoReset"))
			return "CamZoomNoReset";
		if (name.equals("FaceDetectionVideo4S"))
			return "FaceDetectionDuringVideo";
		return name;
	}

	public static boolean isFlipswitch(String name) {
		return name.contains("Flipswitch") || name.contains(" FS");
	}

	public static Comparator<String> versionComparator() {
		return new Comparator<String>() {
			public int compare(String o1, String o2) {
				int d = versionScore(o1) - versionScore(o2);
				return d != 0 ? d : 1;
			}
		};
	}

	public static boolean findTweak(String tweakName) {
		boolean find = tweaks.containsKey(tweakName);
		if (!find)
			System.out.println("Can't find tweak named: " + tweakName);
		return find;
	}

	public static void showStat(String tweakName) {
		if (!findTweak(tweakName))
			return;
		System.out.println(tweakName);
		for (Entry<String, Integer> entry : tweaks.get(tweakName).entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
	}

	public static void showGraph(String tweakName) {
		if (!findTweak(tweakName))
			return;
		GraphPanel.constructGraph(tweakName + " Downloads", tweaks.get(tweakName).values());
	}

	public static Vector<String> readURL(String urlName) throws IOException {
		URL url;
		InputStream is = null;
		BufferedReader br;
		String line;
		StringBuilder sb = new StringBuilder();
		Vector<String> list = new Vector<String>();
		try {
			url = new URL(urlName);
			is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null)
				sb.append(line);
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}
		Pattern p = Pattern.compile("\\<td class\\=\"detail\"\\>([\\w\\d\\.\\+\\(\\)\\'\\- ]+)\\<\\/td\\>\\s+\\<td class\\=\"key\"\\>\\<\\/td\\>\\s+\\<td class\\=\"key\"\\>([\\d\\.-]+)\\<\\/td\\>\\s+\\<td class\\=\"detail\"\\>([\\d,]+)\\<\\/td\\>");
		Matcher m = p.matcher(sb.toString());
		while (m.find())
			list.add(m.group(1) + " " + m.group(2) + " " + m.group(3));
		return list;
	}

	public static void readTweaks() throws IOException {
		String line, name, version, downloads;
		String[] tuple;
		BufferedReader reader = fetchFromLink ? null : new BufferedReader(new FileReader(new File(filename)));
		Vector<String> list = fetchFromLink ? readURL(URL) : null;
		// Skip first 4 lines
		int i = 0;
		if (list == null)
			while (++i <= 4 && reader.readLine() != null);
		while ((line = fetchFromLink ? (i < list.size() ? list.get(i++) : null) : reader.readLine()) != null) {
			line = line.replaceAll(" \\(.* from Installer\\)", "");
			line = (new StringBuilder(line)).reverse().toString();
			line = line.replaceFirst("\\s+", "*").replaceFirst("\\s+", "*");
			line = (new StringBuilder(line)).reverse().toString();
			tuple = line.split("\\*");
			name = normalizedName(tuple[0]);
			if (name == null)
				continue;
			version = normalizedVersion(tuple[1]);
			downloads = tuple[2].replaceAll(",", "");
			// System.out.println(name + " / " + version + " / " + downloads);
			boolean fs = isFlipswitch(name);
			int d = toInt(downloads);
			if (!tweaks.containsKey(name)) {
				tweaks.put(name, new TreeMap<String, Integer>(versionComparator()));
				if (fs)
					flipswitches.put(name, new TreeMap<String, Integer>(versionComparator()));
			}
			tweaks.get(name).put(version, tweaks.get(name).getOrDefault(version, 0) + d);
			submits.put(name + "|" + version, submits.getOrDefault(name + "|" + version, 0) + d);
			if (fs)
				flipswitches.get(name).put(version, d);
			totalDownloads += d;
		}
		if (reader != null)
			reader.close();
		System.out.println("Imported " + tweaks.size() + " tweaks by " + developer + " (BigBoss)");
		System.out.println("Containing " + flipswitches.size() + " flipswitches (BigBoss)");
	}

	public static void setOutdatedTweaks() {
		outdatedTweaks.add("NativeZoom");
		outdatedTweaks.add("Randomy");
		outdatedTweaks.add("MyAssistive");
		outdatedTweaks.add("FB Chat Heads Enabler");
		outdatedTweaks.add("Emoji83+");
		outdatedTweaks.add("CCFlashLightLevel");
	}

	public static <K, V extends Comparable<? super V>> SortedSet<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
		SortedSet<Entry<K, V>> sortedEntries = new TreeSet<Entry<K, V>>(new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				int res = e2.getValue().compareTo(e1.getValue());
				return res != 0 ? res : 1;
			}
		});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	public static void rankBest(int limit, boolean fs) {
		Map<String, Integer> scores = new HashMap<String, Integer>();
		Set<Entry<String, Map<String, Integer>>> set = fs ? flipswitches.entrySet() : tweaks.entrySet();
		for (Entry<String, Map<String, Integer>> entry : set) {
			String tweakName = entry.getKey();
			Map<String, Integer> tweakScores = entry.getValue();
			scores.put(tweakName, 0);
			for (Integer score : tweakScores.values())
				scores.put(tweakName, score + scores.get(tweakName));
		}
		Set<Entry<String, Integer>> sortedTweaks = entriesSortedByValues(scores);
		limit = Math.min(limit, sortedTweaks.size());
		int i = 1;
		for (Entry<String, Integer> entry : sortedTweaks) {
			System.out.println(String.format("%2d. %-" + (fs ? 35 : 25) + "s (%" + (fs ? 5 : 7) + "d downloads)", i,
					entry.getKey(), entry.getValue()));
			i++;
			if (i > limit && limit != -1)
				break;
		}
	}

	public static void showSorted() {
		List<String> tweakNames = new Vector<String>(tweaks.keySet());
		Collections.sort(tweakNames);
		for (int i = 1; i <= tweakNames.size(); i++)
			System.out.println(String.format("%3d. %s", i, tweakNames.get(i - 1)));
	}

	public static void rankBest(int limit) {
		rankBest(limit, false);
	}

	public static void rankSubmitBest(int limit) {
		Set<Entry<String, Integer>> sortedSubmits = entriesSortedByValues(submits);
		limit = Math.min(limit, sortedSubmits.size());
		int i = 1;
		for (Entry<String, Integer> entry : sortedSubmits) {
			String[] tuple = entry.getKey().split("\\|");
			System.out.println(
					String.format("%2d. %-30s (%-6s): %7d downloads", i, tuple[0], tuple[1], entry.getValue()));
			i++;
			if (i > limit && limit != -1)
				break;
		}
	}

	public static void showGraphs(List<String> myTweaks) {
		StringBuilder sb = new StringBuilder();
		List<List<Integer>> _tweaks = new Vector<>();
		for (String tweakName : myTweaks) {
			sb.append(" + " + tweakName);
			_tweaks.add(new Vector<>(tweaks.get(tweakName).values()));
		}
		GraphPanel._constructGraphs(sb.toString().replaceFirst(" \\+ ", ""), _tweaks);
	}

	public static void distTweakDownloads() {
		int block = 1000;
		Integer max = 0;
		Integer min = Integer.MAX_VALUE;
		Map<String, Integer> scores = new HashMap<String, Integer>();
		Set<Entry<String, Map<String, Integer>>> set = tweaks.entrySet();
		for (Entry<String, Map<String, Integer>> entry : set) {
			String tweakName = entry.getKey();
			Map<String, Integer> tweakScores = entry.getValue();
			scores.put(tweakName, 0);
			for (Integer score : tweakScores.values())
				scores.put(tweakName, score + scores.get(tweakName));
			Integer tweakTotalScore = scores.get(tweakName);
			min = Math.min(min, tweakTotalScore);
			max = Math.max(max, tweakTotalScore);
		}
		int width = (int) ((max - min) / block);
		System.out.println("Range: " + width);
		Map<Integer, Integer> dist = new TreeMap<Integer, Integer>();
		for (Integer score : scores.values())
			dist.put(score / width, 1 + dist.getOrDefault(score / width, 0));
		GraphPanel.xMultiplier = (max - min) / dist.size();
		GraphPanel.constructGraph("Downloads Distribution (per tweak) by " + developer, dist.values());
	}

	public static void distDownloads() {
		int block = 500;
		Integer max = 0;
		Integer min = Integer.MAX_VALUE;
		Set<Entry<String, Integer>> sortedSubmits = entriesSortedByValues(submits);
		for (Entry<String, Integer> entry : sortedSubmits) {
			min = Math.min(min, entry.getValue());
			max = Math.max(max, entry.getValue());
		}
		int width = (int) ((max - min) / block);
		System.out.println("Range: " + width);
		Map<Integer, Integer> dist = new TreeMap<Integer, Integer>();
		for (Entry<String, Integer> entry : sortedSubmits) {
			Integer score = entry.getValue();
			dist.put(score / width, 1 + dist.getOrDefault(score / width, 0));
		}
		GraphPanel.xMultiplier = (max - min) / dist.size();
		GraphPanel.constructGraph("Downloads Distribution (per submission) by " + developer, dist.values());
	}

	public static void main(String[] args) throws IOException {
		setOutdatedTweaks();
		readTweaks();
		rankBest(30);
		System.out.println("Total downloads for every submission: " + totalDownloads);
	}

}
