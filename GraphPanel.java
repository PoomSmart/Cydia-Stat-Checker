
/**
 * 
 * A modified version of GraphPanel by PoomSmart
 * Original link: https://gist.github.com/roooodcastro/6325153
 * 
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int width = 1200;
	private int height = 400;
	private int padding = 25;
	private int labelPadding = 25;
	private Color pointColor = new Color(100, 100, 100, 180);
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 4;
	private int numberYDivisions = 20;
	private List<List<Integer>> scores;
	private int currentMaxIndex = 0;
	private int currentMaxVersions = 0;
	private static Random random = new Random();
	public static boolean chart = false;
	public static String[] data;
	private Map<String, Color> colors = new HashMap<String, Color>();
	private Integer maxScore = Integer.MIN_VALUE;

	public static double xMultiplier = 1;

	public GraphPanel(List<List<Integer>> scores) {
		this.scores = scores;
		for (List<Integer> subscores : scores) {
			if (currentMaxVersions < subscores.size()) {
				currentMaxIndex = scores.indexOf(subscores);
				currentMaxVersions = subscores.size();
			}
		}
	}
	
	public Color getColor(int i) {
		String key = data[i].split("|")[0];
		Color color = colors.get(key);
		if (color == null) {
			color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
			colors.put(key, color);
		}
		return color;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.get(currentMaxIndex).size() - 1);
		double yScale = ((double) getHeight() - (2 * padding) - labelPadding) / getMaxScore();

		List<List<Point>> graphPoints = new Vector<>();
		
		int baseY = getHeight() - padding - labelPadding;

		// Adding points
		for (List<Integer> subscores : scores) {
			List<Point> subgraphPoints = new Vector<>();
			for (int i = 0; i < subscores.size(); i++) {
				int x1 = (int) (i * xScale + padding + labelPadding);
				int y1 = baseY - (int) (subscores.get(i) * yScale);
				subgraphPoints.add(new Point(x1, y1));
			}
			graphPoints.add(subgraphPoints);
		}

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - (2 * padding) - labelPadding);
		g2.setColor(Color.BLACK);

		// create hatch marks and grid lines for y axis.
		for (int i = 0; i < numberYDivisions + 1; i++) {
			int x0 = padding + labelPadding;
			int x1 = pointWidth + padding + labelPadding;
			int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
			int y1 = y0;
			if (scores.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
				g2.setColor(Color.BLACK);
				String yLabel = ((int) ((getMaxScore()) * ((i * 1.0) / numberYDivisions) * 100)) / 100 + "";
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
			}
			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		for (int i = 0; i < scores.get(currentMaxIndex).size(); i++) {
			if (scores.get(currentMaxIndex).size() > 1) {
				int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.get(currentMaxIndex).size() - 1)
						+ padding + labelPadding;
				int x1 = x0;
				int y0 = getHeight() - padding - labelPadding;
				int y1 = y0 - pointWidth;
				if ((i % ((int) ((scores.get(currentMaxIndex).size() / 20.0)) + 1)) == 0) {
					g2.setColor(gridColor);
					g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
					g2.setColor(Color.BLACK);
					String xLabel = (int) (i * xMultiplier) + "";
					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
				}
				g2.drawLine(x0, y0, x1, y1);
			}
		}

		// create x and y axes
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding,
				getHeight() - padding - labelPadding);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(GRAPH_STROKE);
		if (chart) {
			for (List<Point> subgraphPoints : graphPoints) {
				for (int i = 0; i < subgraphPoints.size(); i++) {
					g2.setColor(getColor(i));
					int x = subgraphPoints.get(i).x;
					int y = subgraphPoints.get(i).y;
					g2.drawLine(x, baseY, x, y);
				}
			}
		} else {
			for (List<Point> subgraphPoints : graphPoints) {
				g2.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
				for (int i = 0; i < subgraphPoints.size() - 1; i++) {
					int x1 = subgraphPoints.get(i).x;
					int y1 = subgraphPoints.get(i).y;
					int x2 = subgraphPoints.get(i + 1).x;
					int y2 = subgraphPoints.get(i + 1).y;
					g2.drawLine(x1, y1, x2, y2);
				}
			}
		}

		g2.setStroke(oldStroke);
		g2.setColor(pointColor);
		for (List<Point> subgraphPoints : graphPoints) {
			for (int i = 0; i < subgraphPoints.size(); i++) {
				int x = subgraphPoints.get(i).x - pointWidth / 2;
				int y = subgraphPoints.get(i).y - pointWidth / 2;
				int ovalW = pointWidth;
				int ovalH = pointWidth;
				g2.fillOval(x, y, ovalW, ovalH);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	private Integer getMinScore() {
		Integer minScore = Integer.MAX_VALUE;
		for (List<Integer> subscores : scores) {
			for (Integer score : subscores)
				minScore = Math.min(minScore, score);
		}
		return minScore;
	}

	private Integer getMaxScore() {
		if (maxScore != Integer.MIN_VALUE)
			return maxScore;
		for (List<Integer> subscores : scores) {
			for (Integer score : subscores)
				maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}

	public void setScores(List<List<Integer>> scores) {
		this.scores = scores;
		invalidate();
		this.repaint();
	}

	public List<List<Integer>> getScores() {
		return scores;
	}

	public static void _constructGraphs(String name, List<List<Integer>> scores) {
		GraphPanel mainPanel = new GraphPanel(scores);
		if (name == null)
			name = "Multiple tweaks";
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void constructGraphs(List<List<Integer>> scores) {
		_constructGraphs(null, scores);
	}

	public static void constructGraph(String name, Collection<Integer> downloads) {
		List<List<Integer>> scores = new Vector<>();
		List<Integer> subscores = new Vector<>();
		for (Integer d : downloads)
			subscores.add(d);
		scores.add(subscores);
		_constructGraphs(name, scores);
	}
}