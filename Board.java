  public class Board {
      public Board(int[][] blocks)           // construct a board from an N-by-N array of blocks
                                             // (where blocks[i][j] = block in row i, column j)
      public int dimension()                 // board dimension N
      public int hamming()                   // number of blocks out of place
      public int manhattan()                 // sum of Manhattan distances between blocks and goal
      public boolean isGoal()                // is this board the goal board?
      public Board twin()                    // a board obtained by exchanging two adjacent blocks in the same row
      public boolean equals(Object y)        // does this board equal y?
      public Iterable<Board> neighbors()     // all neighboring boards
      public String toString()               // string representation of the board 
  }






import java.util.Arrays;

public class Board {

    private int N;                                  // dimension of board
    private int[] board;                         // board of interest

    public Board(int[][] blocks)  {         // construct a board from an N-by-N array of blocks
        N = blocks[0].length;
        board = new int[N * N];
        for (int i = 0; i < N; i++)           // blocks[i][j]: block in row i, column j
            for (int j = 0; j < N; j++)
                board[i * N + j] = blocks[i][j];
    }

    private Board(int[] board) {            // private constructor useful in twin()
        N = (int) Math.sqrt(board.length);
        this.board = new int[board.length];
        for (int i = 0; i < board.length; i++)
            this.board[i] = board[i];
    }

    public int dimension() {                 // board dimension N
        return N;
    }

    public int hamming() {                  // number of blocks out of place
        int count = 0;
        for (int i = 0; i < N * N; i++)      // compare board[1] through board[N^2-1] with goal
                if (board[i] != i + 1 && board[i] != 0)                  // count for blocks in wrong place
                    count++;
        return count;
    }

    public int manhattan() {               // sum of Manhattan distances between blocks and goal
        int sum = 0;
        for (int i = 0; i < N * N; i++)
            if (board[i] != i + 1 && board[i] != 0)
                sum += manhattan(board[i], i);
        return sum;
    }

    private int manhattan(int goal, int current) {  // return manhattan distance of a misplaced block
        int row, col;                                                // row and column distance from the goal
        row = Math.abs((goal - 1) / N - current / N);              // row difference
        col = Math.abs((goal - 1) % N - current % N);             // column difference
        return row + col;
    }

    public boolean isGoal() {              // is this board the goal board?
        for (int i = 0; i < N * N - 1; i++)
             if (board[i] != i + 1) 
                 return false;
        return true;
    }

    public Board twin() {                  // a board obtained by exchanging two adjacent blocks in the same row
        Board twin;
        if (N == 1)  return null;                        // check if twin board exists
        twin = new Board(board);

        if (board[0] != 0 && board[1] != 0)
            exch(twin, 0, 1);                // if the first two blocks in first row is not empty, exchange them.
        else
            exch(twin, N, N + 1);  // otherwise, exchange the first two blocks on second row.
        return twin;
    }

    private Board exch(Board a, int i, int j) { // exchange two elements in the array
        int temp = a.board[i];
        a.board[j] = a.board[i];
        a.board[i] = temp;
        return a;
    }

    public boolean equals(Object y) {      // does this board equal y?
        if (y == this)  return true;
        if (y == null)  return false;
        if (y.getClass() != this.getClass()) return false;

        Board that = (Board) y;
        return Arrays.equals(this.board, that.board);
    }

    public Iterable<Board> neighbors() {    // all neighboring boards
        int index = 0;                               // record the position of empty block
        boolean found = false;                       // if empty block is found
        Board neighbor;
        Queue<Board> q = new Queue<Board>();

        for (int i = 0; i < board.length; i++)    // search for empty block
            if (board[i] == 0) {
                index = i;
                found = true;
                break;
            }
        if (!found)  return null;

        if (index / N != 0) {                      // if not first row
            neighbor = new Board(board);
            exch(neighbor, index, index - N);  // exchange with upper block
            q.enqueue(neighbor);
        }

        if (index / N != (N - 1)) {               // if not last row
            neighbor = new Board(board);
            exch(neighbor, index, index + N);  // exchange with lower block
            q.enqueue(neighbor);
        }

        if ((index % N) != 0) {                        // if not leftmost column
            neighbor = new Board(board);
            exch(neighbor, index, index - 1);  // exchange with left block
            q.enqueue(neighbor);
        }

        if ((index % N) != N - 1) {                          // if not rightmost column
            neighbor = new Board(board);
            exch(neighbor, index, index + 1);  // exchange with left block
            q.enqueue(neighbor);
        }

        return q;
    }

    public String toString() {              // string representation of the board
        StringBuilder s = new StringBuilder();
        s.append(N + "\n");
        for (int i = 0; i < board.length; i++) {
            s.append(String.format("%2d ", board[i]));
            if (i % N == 0)
                s.append("\n");
        }
        return s.toString();
    }
}


/*******************************************************************************
* Compilation: javac Solver.java
* Execution:
* Dependencies: Board.java, algs4.jar, java.util, stdlib.jar
*
* This program creates an immutable data type that solves
* 8-puzzle problem using A* algorithm.
*******************************************************************************/
import java.util.Comparator;

public class Solver {
    private SearchNode goal;              
    
    private class SearchNode {             // A search node consists of the board, number of moves to reach
        private int moves;                 // this step and pointed to the previous search node
        private Board board;
        private SearchNode prev;

        public SearchNode(Board initial) {
            moves = 0;
            prev = null;
            board = initial;
        }
    }

    public Solver(Board initial) {           // find a solution to the initial board (using the A* algorithm)
        PriorityOrder order = new PriorityOrder();
        MinPQ<SearchNode> PQ = new MinPQ<SearchNode>(order);
        MinPQ<SearchNode> twinPQ = new MinPQ<SearchNode>(order);
        SearchNode Node = new SearchNode(initial);
        SearchNode twinNode = new SearchNode(initial);
        PQ.insert(Node);
        twinPQ.insert(twinNode);                // twin created to detect infeasible cases

        SearchNode min = PQ.delMin();
        SearchNode twinMin = twinPQ.delMin();

        while(!min.board.isGoal() && !twinMin.board.isGoal()) {

            for (Board b : min.board.neighbors()) {      
                if (min.prev == null || !b.equals(min.prev.board)) {   // check if move back this previous state
                    SearchNode n = new SearchNode(b);
                    n.moves = min.moves + 1;
                    n.prev = min;
                    PQ.insert(n);
                    }
            }
            
            for (Board b : twinMin.board.neighbors()) {
                if (twinMin.prev == null ||!b.equals(twinMin.prev.board)) {
                    SearchNode n = new SearchNode(b);
                    n.moves = twinMin.moves + 1;
                    n.prev = twinMin;
                    twinPQ.insert(n);
                    }
            }
             
             min = PQ.delMin();
             twinMin = twinPQ.delMin();
         }
         if (min.board.isGoal())  goal = min;
         else                     goal = null;                
    }

    private class PriorityOrder implements Comparator<SearchNode> {
        public int compare(SearchNode a, SearchNode b) {
            int pa = a.board.manhattan() + a.moves;
            int pb = b.board.manhattan() + b.moves;
            if (pa > pb)   return 1;
            if (pa < pb)   return -1;
            else              return 0;
        }
    }

    public boolean isSolvable() {            // is the initial board solvable?
        return goal != null;
    }

    public int moves() {                     // min number of moves to solve initial board; -1 if no solution
        if (!isSolvable())  return -1;
        else                   return goal.moves;
    }

    public Iterable<Board> solution() {      // sequence of boards in a shortest solution; null if no solution
        if (!isSolvable())  return null;
        Stack<Board> s = new Stack<Board>();
        for (SearchNode n = goal; n != null; n = n.prev) 
            s.push(n.board);
        return s;
    }

    public static void main(String[] args) { // solve a slider puzzle
        // create initial board from file
        In in = new In(args[0]);
        int N = in.readInt();
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                blocks[i][j] = in.readInt();
        Board initial = new Board(blocks);

        // solve the puzzle
        Solver solver = new Solver(initial);

        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
