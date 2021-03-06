package L3Int_4_graph;

import L3Int_4_graph.exceptions.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.FileReader;

public final class Graph {

	/**
	 * Number of vertices that the graph contains
	 */
	private int numberVertices;

	/**
	 * Array of vertices that the graph contains
	 */
	private ArrayList<Vertex> vertices;


	/**
	 * Create a graph based on its structure (contained in a text file)
	 * 
	 * @param filePath Path to the file to open
	 * @throws FileNotFoundException     If the passed file does not exist
	 * @throws InvalidGraphFileException If the passed file can not be used for
	 *                                   instantiating a new {@link Graph}
	 */
	public Graph(String filePath) throws FileNotFoundException, InvalidGraphFileException {
		// Resetting the IDs of the Vertices
		Vertex.resetIds(); // Hence avoiding collision

		Scanner sc = new Scanner(new FileReader(filePath)); // Scanner on the file, throw a FileNotFound exception if needed

		// Attempt to read the first line (number of vertices in the graph)
		this.numberVertices = readNumberVertices(sc);

		this.vertices = new ArrayList<>();
		for (int i = 0; i < this.numberVertices; i++) {
			this.vertices.add(new Vertex());
		}

		// Going through all edges described by the file and adding them to the new
		// instance
		while (sc.hasNextLine()) {
			this.addEdge(sc);
		}

		sc.close();
	}


	public int getNbVertices() {
		return numberVertices;
	}


	public ArrayList<Vertex> getVertices() {
		return vertices;
	}


	/**
	 * Add an edge to the graph, based on the {@link Scanner} on the building file
	 * 
	 * @param sc {@link Scanner} on the building file
	 * @throws InvalidGraphFileException If a line does not have the right format
	 */
	private void addEdge(Scanner sc) throws InvalidGraphFileException {
		// Creating a new Scanner based on the line to parse
		try (Scanner sc2 = new Scanner(sc.nextLine())) {
			int from = sc2.nextInt(); // Reading the start vertex of the Edge
			int weight = sc2.nextInt(); // Reading the weight of the Edge
			int to = sc2.nextInt(); // Reading the end vertex of the Edge

			// Securing inputs
			if (from < 0 || from >= this.numberVertices || to < 0 || to >= this.numberVertices) {
				throw new InvalidGraphFileException(
						"A line describing an Edge is faulty (wrong parameters): unable to build a Graph based on that file");
			}

			// If no error has been thrown here, we can add the Edge to the graph
			Edge edge = new Edge(this.vertices.get(from), this.vertices.get(to), weight); // Actually creating the Edge to add
			this.vertices.get(from).addOutEdge(edge);
			this.vertices.get(to).addInEdge(edge);

		} catch (NoSuchElementException e) {
			throw new InvalidGraphFileException(
					"A line describing an Edge is faulty (wrong structure): unable to build a Graph based on that file");
		}
	}


	/**
	 * Get a reference to a {@link Vertex} by its ID
	 * @param vertexID ID of the {@link Vertex} to get the reference to
	 * @return Reference to the asked {@link Vertex} or {@code null} if not found
	 */
	public Vertex getVertex (int vertexID) {
		for (Vertex v : this.vertices) {
			if (v.getId() == vertexID) {
				return v;
			}
		}

		return null;
	}


	/**
	 * Display the adjacency matrix of the current graph
	 */
	public void displayAdjacencyMatrix() {

		System.out.println("=== ADJACENCY MATRIX ===\n");

		// First, we display the top line of the table
		System.out.print("   ║");
		for (int i = 0; i < this.vertices.size(); i++) {
			System.out.print(String.format(" %d ║", i));
		}
		System.out.print("\n");

		// Then we display a separator line
		for (int i = -2; i < (this.vertices.size() / 10) + 1; i++) {
			System.out.print("═");
		}
		System.out.print("║");
		for (int i = 0; i < this.vertices.size(); i++) {
			System.out.print("═");
			for (int j = 0; j < ("" + i).length(); j++) {
				System.out.print("═");
			}
			System.out.print("═║");
		}
		System.out.print("\n");

		// Now, we check adjacency for each vertex
		for (Vertex v : this.vertices) {
			// Print the ID of the current vertice
			System.out.print(String.format(" %d ║", v.getId()));

			// Check the successors of the current vertice
			for (Vertex vertex : this.vertices) {
				String link = v.hasSuccessor(vertex) ? " 1 " : "   ";
				System.out.print(link + "║");
			}

			System.out.print("\n");
		}
	}


	/**
	 * Get a {@link String} representation of the instance of the {@link Graph}
	 */
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		// First, we display the top line of the table
		ret.append("     ║");
		for (int i = 0; i < this.vertices.size(); i++) {
			ret.append(String.format(" %4d ║", i));
		}
		ret.append("\n");

		// Then we display a separator line
		for (int i = -2; i < (this.vertices.size() / 10) + 3; i++) {
			ret.append("═");
		}
		ret.append("║");
		for (int i = 0; i < this.vertices.size(); i++) {
			ret.append("══════║");
		}
		ret.append("\n");

		// Now, we check adjacency for each vertex
		for (Vertex v : this.vertices) {
			// Print the ID of the current vertice
			ret.append(String.format(" %3d ║", v.getId()));

			// Check the successors of the current vertice
			for (Vertex vertex : this.vertices) {
				String link = null;
				try {
					link = v.hasSuccessor(vertex) ? String.format(" %4d ", v.getWeightTo(vertex)) : "      ";
				} catch (NotLinkedException ignored) {}
				ret.append(link).append("║");
			}

			ret.append("\n");
		}

		return ret.toString();
	}


	/**
	 * Check if the instance contains a {@link Edge} with a negative weight
	 * 
	 * @return Whether or not the instance contains a {@link Edge} with a negative
	 *         weight
	 */
	public boolean hasNegativeEdge() {
		// Check for all vertices of the graph
		for (Vertex v : this.vertices) {
			if (v.hasNegativeLink()) {
				return true; // If the current Vertex contains an Edge with a negative weight
			}
		}

		return false; // If none
	}


	/**
	 * Check whether or not the instance contains an absorbent cycle
	 * 
	 * @return Whether or not the instance contains an absorbent cycle
	 */
	public boolean hasAbsorbentCycle() {
		for (Vertex v : this.vertices) {
			if (hasAbsorbentCycle(v, v, 0, 0, new ArrayList<>())) {
				return true;
			}
		}

		return false;
	}


	/**
	 * TODO: Check speed compared to Bellman-Ford algorithm
	 * Recursive method that checks whether or not there is an absorbent cycle
	 * starting from the given Vertex
	 * 
	 * @param start       Reference to the {@link Vertex} at the start of the
	 *                    potential cycle
	 * @param current     Reference to the {@link Vertex} currently being analyzed
	 * @param cycleWeight Weight of the potential cycle being analyzed
	 * @param depth       Number of iterations (call with {@code 0} to start)
	 * @param cleared     {@link ArrayList} of {@link Vertex} acting as cache for
	 *                    already checked vertices
	 * @return Whether or not the {@code start} {@link Vertex} is the start of an
	 *         absorbent cycle
	 */
	private boolean hasAbsorbentCycle(Vertex start, Vertex current, int cycleWeight, int depth,	ArrayList<Vertex> cleared) {
		// If we looped back to the start, check the cycle weight
		if (current == start && depth != 0) {
			return cycleWeight < 0;
		}

		// Recursive call for each outgoing Edge of the current Vertex
		for (Edge e : current.getOutEdge()) {
			if (!cleared.contains(e.getEndVertex())
					&& hasAbsorbentCycle(start, e.getEndVertex(), cycleWeight + e.getWeight(), depth++, cleared)) {
				return true;
			}
		}

		// Add the current Vertex to the cache (already checked vertices)
		cleared.add(current);

		// By default, there is no absorbent cycle
		return false;
	}


	/**
	 * Read the first line of the passed file, check the value and act consequently
	 * 
	 * @param sc {@link Scanner} on the file to be read
	 * @return Number of vertices in the graph according to the file
	 * @throws InvalidGraphFileException If the file does not have the right format,
	 *                                   structure or if data are not realistic
	 *                                   (negative number of vertices)
	 */
	private static int readNumberVertices(Scanner sc) throws InvalidGraphFileException {
		final int MINIMUM_NUMBER_OF_VERTICES = 1;

		if (sc.hasNextInt()) { // Check if able to read an integer from the scanner
			int numberOfVertices = sc.nextInt();

			// Check read value
			if (numberOfVertices < MINIMUM_NUMBER_OF_VERTICES) {
				throw new InvalidGraphFileException("A graph must contain at least " + MINIMUM_NUMBER_OF_VERTICES
						+ " vertices, but this file specifies " + numberOfVertices);
			}

			sc.nextLine(); // Escaping the end line character

			return numberOfVertices; // Now safe to return

		} else { // Throw an InvalidGraphFileException if not able to read
			throw new InvalidGraphFileException("Cannot read number of vertices from the passed file");
		}
	}


	/**
	 * Apply the Bellman-Ford algorithm to the graph using the passed the {@link Vertex} with the passed {@code id} ({@code vertexId})
	 * @param vertexId Id of the {@link Vertex} to use as the source
	 */
	public void applyBellmanFord(int vertexId) {
		if (vertexId < 0 || vertexId >= this.vertices.size()) {
			System.out.println("You cannot use this ID to start, as this vertex does not exist in this graph");
		} else {
			applyBellmanFord(this.getVertex(vertexId));
		}
	}


	/**
	 * Apply the Bellman-Ford algorithm to the graph, starting with a passed in
	 * source {@link Vertex}
	 */
	public void applyBellmanFord(Vertex startVertex) {
		final double NULL_DISTANCE = 0.0; // #NoMagicNumbers
		final int MAX_ITERATIONS = this.vertices.size() - 1;

		Map<Vertex, ArrayList<Vertex>> paths = new HashMap<>();

		// Initialize distances to all vertices to infinity (positive infinity)
		Map<Vertex, Double> distances = new HashMap<>();
		for (Vertex v : this.vertices) {
			distances.put(v, Double.POSITIVE_INFINITY);
		}
		distances.replace(startVertex, NULL_DISTANCE);

		// Initialize paths (explored vertices and how to get to them)
		paths.put(startVertex, new ArrayList<>());
		paths.get(startVertex).add(startVertex);

		ArrayList<Vertex> modified = new ArrayList<>();
		modified.add(startVertex);

		this.displayBellmanFordStatusTableFirstLine(startVertex);

		for (int i = 0; i <= MAX_ITERATIONS; i++) { // Loop at most n times, then check for absorbent loops
			ArrayList<Vertex> modifiedLast = new ArrayList<>(modified);
			Map<Vertex, Double> lastDistances = new HashMap<>(distances);
			modified.clear(); // Reset the modified vertices

			// Explore all successors of all reached vertices
			for (Vertex v : modifiedLast) {
				for (Vertex successor : v.getSuccessors()) {

					// Get the distance to successor by pathing through v
					double newDistance;

					try {
						newDistance = distances.get(v) + v.getWeightTo(successor);
					} catch (NotLinkedException e) {  // Will not happen at all (because of the way this method is built)
						System.err.println("Attempted to access unlinked Vertex");
						continue;  // Avoid using a 0 value
					}
					

					if (paths.containsKey(successor)) {  // If Vertex has already been reached
						// Check if "distance" smaller than stored one
						if (newDistance < distances.get(successor)) {
							// Update distance to Vertex
							distances.replace(successor, newDistance);

							// Update path to Vertex
							paths.replace(successor, new ArrayList<>(paths.get(v)));
							paths.get(successor).add(successor);

							// Add Vertex to currently modified vertices
							modified.add(successor);
						}
						
					} else {  // If Vertex is reached for the first time

						// Set distance to Vertex
						distances.replace(successor, newDistance);
						
						// Set path to Vertex
						paths.put(successor, new ArrayList<>(paths.get(v)));
						paths.get(successor).add(successor);

						// Add Vertex to currently modified vertices
						modified.add(successor);
					}
				}
			}


			// Display state of distances after operations made at this step
			System.out.printf("Step %3d) |  ", i+1);
			for (Vertex v : this.vertices) {
				if (distances.get(v) == Double.POSITIVE_INFINITY) {
					System.out.print("  +inf.  | ");
				} else {
					Vertex reachingVertex = paths.get(v).size() <= 1 ? v : paths.get(v).get(paths.get(v).size() - 2);
					System.out.printf(" %3.0f(%d)  | ", distances.get(v), reachingVertex.getId());
				}
			}
			System.out.println();  // Linebreak

			if (i == MAX_ITERATIONS && !distances.equals(lastDistances)) {
				System.out.println();  // Linebreak
				System.out.println("Absorbent cycle detected in this graph. The Bellman algorithm will not continue.");
			}
		}
	}


	/**
	 * Display the first line of the status table for the Bellman-Ford algorithm
	 * @param sourceVertex {@link Vertex} to use as the source vertex
	 */
	private void displayBellmanFordStatusTableFirstLine(Vertex sourceVertex) {
		System.out.println("Applying Bellman-Ford algorithm using " + sourceVertex.getId() + " as the source vertex");

		System.out.print("Vertex id | ");
		for (Vertex v : this.vertices) {
			System.out.printf("   %3d    |", v.getId());
		}
		System.out.println();
	}


	/**
	 * Apply the Dijkstra algorithm to find the shortest path from a {@link Vertex} to all accessible others
	 * @param vertexId Id of the {@link Vertex} to use as the source Vertex
	 */
	public void applyDijkstra(int vertexId) {
		boolean perform = true;

		if (this.hasNegativeEdge()) {
			System.out.println("You cannot perform Dijkstra's algorithm on this graph as it contains a negative weighted edge");
			perform = false;
		}

		if (vertexId < 0 || vertexId >= this.vertices.size()) {
			System.out.println("You cannot use this ID to start, as this vertex does not exist in this graph");
			perform = false;
		}

		if (perform) {
			applyDijkstra(this.vertices.get(vertexId));
		}
	}


	/**
	 * Apply the Dijkstra algorithm to find the shortest path from a {@link Vertex} to all accessible others
	 * @param sourceVertex Reference to the {@link Vertex} to use as the source Vertex
	 */
	private void applyDijkstra(Vertex sourceVertex) {
		// Creating and initializing the distances to each node from the source
		Map<Vertex, Double> distances = new HashMap<>();
		for (Vertex v : this.vertices) {
			distances.put(v, v == sourceVertex ? 0 : Double.POSITIVE_INFINITY);  // Distance = 0 if source vertex, positive infinity otherwise
		}

		// Visited vertices (so that they are not checked again), initialized with only the source vertex
		ArrayList<Vertex> visitedVertices = new ArrayList<>();
		visitedVertices.add(sourceVertex);

		displayDijkstraStatusFirstLine(sourceVertex);

		// Exploring all vertices that can be visited
		for (int i = 0; i < visitedVertices.size(); i++) {
			Vertex currentVertex = visitedVertices.get(i);

			// Exploring all successors of the current vertex
			for (Vertex v : currentVertex.getSuccessors()) {
				// Gather the weight of the edge linking both vertices
				double edgeWeight;
				try {
					edgeWeight = currentVertex.getWeightTo(v);
				} catch (NotLinkedException e) {
					System.err.println("An error occurred while performing Dijkstra's algorithm");
					continue;  // So that we do not use a falsy value for the weight of the edge (we just do not consider it)
				}

				// Update distance if needed
				if (distances.get(currentVertex) + edgeWeight < distances.get(v)) {
					distances.replace(v, distances.get(currentVertex) + edgeWeight);
				}

				// Adding the Vertex to the visited vertices only if it is not already in (to avoid infinite loops)
				if (!visitedVertices.contains(v)) {
					visitedVertices.add(v);
				}
			}

			this.displayDijkstraStatusLine(distances, visitedVertices);
		}
	}


	/**
	 * Display the current state of distances from the source in the {@code applyDijkstra} method
	 * @param distances Map associating each vertex to its optimal distance from the source
	 * @param visitedVertices List of visited vertices
	 */
	private void displayDijkstraStatusLine(Map<Vertex, Double> distances, ArrayList<Vertex> visitedVertices) {
		System.out.print("|");
		for (Vertex v : this.vertices) {
			if (distances.get(v) == Double.POSITIVE_INFINITY) {
				System.out.print("  +inf.  |");
			} else {
				System.out.printf(" %7.0f |", distances.get(v));
			}
		}
		for (Vertex visited : visitedVertices) {
			System.out.printf(" %d ", visited.getId());
		}
		System.out.println();
	}


	/**
	 * Display the first line of the Dijkstra status table (for code refactoring)
	 * @param sourceVertex {@link Vertex} to use as the source for Dijkstra's algorithm
	 */
	private void displayDijkstraStatusFirstLine(Vertex sourceVertex) {
		System.out.println("Performing Dijkstra's algorithm using vertex #" + sourceVertex.getId() + " as the source");

		System.out.print("|");
		for (Vertex v : this.vertices) {
			System.out.printf(" %7d |", v.getId());
		}
		System.out.println(" CC ");

		for (int i = 0; i < this.vertices.size() * 10 + 4; i++) {
			System.out.print("-");
		}
		System.out.println();
	}
}
