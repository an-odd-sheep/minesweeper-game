import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;


class BoardTile extends JButton
{
    int row;
    int column;

    public BoardTile(int row, int column)
    {
        this.row = row;
        this.column = column;
    }
}


class MineList
{
    Node head = null;
    Node currNode = null;

    class Node
    {
        BoardTile tile;
        Node next;

        Node(BoardTile tile)
        {
            this.tile = tile;
            this.next = null;
        }
    }

    void addTile(BoardTile tile)
    {
        Node newNode = new Node(tile);

        if(head == null)
        {
            head = newNode;
            return;
        }

        newNode.next = head;
        head = newNode;
    }

    boolean contains(BoardTile tile)
    {
        currNode = head;
        while(currNode != null)
        {
            if(currNode.tile == tile)
            {
                return true;
            }
            currNode = currNode.next;
        }

        return false;
    }
}


class LeaderTree
{
    Node root = null;
    Node currNode = null;

    class Node
    {
        String userName;
        int tilesClicked;
        int minesCount;
        Node left;
        Node right;

        Node(String userName, int tilesClicked, int minesCount)
        {
            this.userName = userName;
            this.tilesClicked = tilesClicked;
            this.minesCount = minesCount;
        }
    }

    Node addScore(Node node, String userName, int tilesClicked, int minesCount)
    {
        if(node == null)
        {
            node = new Node(userName, tilesClicked, minesCount);
            node.left = node.right = null;

            if(root == null)
            {
                root = node;
            }
            return node;
        }

        if(tilesClicked >= node.tilesClicked)
        {
            node.left = addScore(node.left, userName, tilesClicked, minesCount);
        }
        else
        {
            node.right = addScore(node.right, userName, tilesClicked, minesCount);
        }

        return node;
    }

    void inOrder(Node node, int minesCount)
    {
        if(node == null)
        {
            return;
        }
        
        inOrder(node.left, minesCount);
        if(node.minesCount == minesCount)
        {
            int totalTiles = (minesCount == 6)? 6*6: ((minesCount == 12)? 9*9: 12*12);
            System.out.println("Username: " + String.format("%-15s", node.userName) + "\tTiles: " + String.format("%-4s", node.tilesClicked + "/" + totalTiles));
        }
        inOrder(node.right, minesCount);

    }
}



public class Minesweeper
{
    int tileSize = 70;
    int numRows = 9;
    int numColumns = numRows;
    int boardWidth = numColumns * tileSize;
    int boardHeight = numRows * tileSize;
    int minesCount = 12;
    int tilesClicked = 0;
    boolean gameOver = false;

    JFrame frame = new JFrame("Minesweeper");;
    JLabel textLabel = new JLabel();
    JComboBox<String> difficultyLevel = new JComboBox<String>(new String[] {"Easy", "Medium", "Hard"});
    JLabel timeLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    BoardTile board[][] = new BoardTile[12][12];
    MineList mineList = new MineList();
    Random random = new Random();
    JMenuBar menuBar = new JMenuBar();
    JMenu leaderMenu = new JMenu("Leaderboard");
    JMenuItem easyMenu = new JMenuItem("Easy");
    JMenuItem mediumMenu = new JMenuItem("Medium");
    JMenuItem hardMenu = new JMenuItem("Hard");
    LeaderTree leaderBoard = new LeaderTree();

    Font menuFont = new Font("Arial", Font.BOLD, 18);
    Font textFont = new Font("Arial Unicode MS", Font.BOLD, 25);
    Font tileFont = new Font("Arial Unicode MS", Font.PLAIN, 45);


    class Timer extends Thread
    {
        int time = 0;

        @Override
        public void run()
        {
            while(true)
            {
                if(!gameOver)
                {
                    timeLabel.setText("Time: " + time);
                    time++;
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException err)
                {
                    gameOver = true;
                }
            }
        }
    }
    Timer timerThread = new Timer();

    class LeaderBoard extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                if(!new File("ScoreBoard.txt").exists())
                {
                    return;
                }

                FileInputStream fis = new FileInputStream("ScoreBoard.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                leaderBoard = new LeaderTree();
                String line;
                while ((line = br.readLine()) != null)
                {
                    String parts[] = line.split(",");
                    if (parts.length == 3)
                    {
                        String username = parts[0].trim();
                        int tilesClicked = Integer.parseInt(parts[1].trim());
                        int minesCount = Integer.parseInt(parts[2].trim());
                        leaderBoard.addScore(leaderBoard.root, username, tilesClicked, minesCount);
                    }
                }
                br.close();
                isr.close();
                fis.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    LeaderBoard leaderThread = new LeaderBoard();


    Minesweeper()
    {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        difficultyLevel.setSelectedIndex(1);
        difficultyLevel.setFont(textFont);
        difficultyLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(e.getSource() == difficultyLevel)
                {
                    gameOver = true;
                    if(difficultyLevel.getSelectedItem() == "Easy")
                    {
                        numRows = 6;
                        tileSize = 80;
                        minesCount = 6;
                    }
                    else if(difficultyLevel.getSelectedItem() == "Medium")
                    {
                        numRows = 9;
                        tileSize = 70;
                        minesCount = 12;
                    }
                    else
                    {
                        numRows = 12;
                        tileSize = 60;
                        minesCount = 24;
                    }
                    setBoard();
                }
            }
        });

        textLabel.setFont(textFont);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Mines: " + minesCount);
        textLabel.setOpaque(true);

        timeLabel.setFont(textFont);
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel.setOpaque(true);

        frame.add(textPanel, BorderLayout.NORTH);
        frame.add(boardPanel);

        leaderMenu.setFont(menuFont);

        easyMenu.setFont(menuFont);
        easyMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("\nEASY LEVEL LEADERBOARD:");
                leaderBoard.inOrder(leaderBoard.root, 6);
            }
        });

        mediumMenu.setFont(menuFont);
        mediumMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("\nMEDIUM LEVEL LEADERBOARD:");
                leaderBoard.inOrder(leaderBoard.root, 12);
            }
        });

        hardMenu.setFont(menuFont);
        hardMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("\nHARD LEVEL LEADERBOARD:");
                leaderBoard.inOrder(leaderBoard.root, 24);
            }
        });

        leaderMenu.add(easyMenu);
        leaderMenu.add(mediumMenu);
        leaderMenu.add(hardMenu);
        menuBar.add(leaderMenu);
        frame.setJMenuBar(menuBar);

        setBoard();
        timerThread.start();
    }

    void setBoard()
    {
        leaderThread = new LeaderBoard();
        leaderThread.start();
        timerThread.time = 0;

        gameOver = false;
        tilesClicked = 0;
        textLabel.setText("Mines: " + minesCount);
        timeLabel.setText("Time: 0");

        numColumns = numRows;
        boardWidth = numColumns * tileSize;
        boardHeight = numRows * tileSize;
        frame.setSize(boardWidth, boardHeight);

        textPanel.setPreferredSize(new Dimension(boardWidth, 50));
        textPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, boardWidth/25, 10, boardWidth/25);
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        textPanel.add(difficultyLevel, gbc);
        gbc.gridx = 1;
        textPanel.add(textLabel, gbc);
        gbc.gridx = 2;
        textPanel.add(timeLabel, gbc);

        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numColumns));

        Arrays.stream(board).forEach(x -> Arrays.fill(x, null));

        for(int r = 0; r < numRows; r++)
        {
            for(int c = 0; c < numColumns; c++)
            {
                BoardTile tile = new BoardTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(tileFont);

                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        if(gameOver)
                        {
                            return;
                        }
                        BoardTile tile = (BoardTile)e.getSource();

                        if(e.getButton() == MouseEvent.BUTTON1)
                        {
                            if(tile.getText() == "" && tile.isEnabled())
                            {
                                if(mineList.contains(tile))
                                {
                                    showMines();
                                }
                                else if(tile.getText() != "🚩")
                                {
                                    countMines(tile.row, tile.column);
                                }
                            }
                        }
                        else if(e.getButton() == MouseEvent.BUTTON3)
                        {
                            if(tile.getText() == "" && tile.isEnabled())
                            {
                                tile.setText("🚩");
                            }
                            else if(tile.getText() == "🚩")
                            {
                                tile.setText("");
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
        frame.setVisible(true);
        setMines();
    }

    void setMines()
    {
        int minesLeft = minesCount;
        while(minesLeft > 0)
        {
            int row = random.nextInt(numRows);
            int column = random.nextInt(numColumns);
            BoardTile tile = board[row][column];
            
            if(!mineList.contains(tile))
            {
                mineList.addTile(tile);
                minesLeft--;
            }
        }

    }

    void showMines()
    {
        mineList.currNode = mineList.head;
        while(mineList.currNode != null)
        {
            BoardTile tile = mineList.currNode.tile;
            tile.setText("💣");
            mineList.currNode = mineList.currNode.next;
        }
        gameOver = true;
        textLabel.setText("Game Over");

        setScore();
    }

    int checkMine(int row, int column)
    {
        if(row < 0 || row >= numRows || column < 0 || column >= numColumns)
        {
            return 0;
        }
        else if(mineList.contains(board[row][column]))
        {
            return 1;
        }
        return 0;
    }

    void countMines(int row, int column)
    {
        if(row < 0 || row >= numRows || column < 0 || column >= numColumns)
        {
            return;
        }

        BoardTile tile = board[row][column];

        if(!tile.isEnabled())
        {
            return;
        }
        tile.setEnabled(false);
        tilesClicked++;

        int numMines = 0;
        numMines += checkMine(row - 1, column - 1);
        numMines += checkMine(row - 1, column);
        numMines += checkMine(row - 1, column + 1);
        numMines += checkMine(row, column - 1);
        numMines += checkMine(row, column + 1);
        numMines += checkMine(row + 1, column - 1);
        numMines += checkMine(row + 1, column);
        numMines += checkMine(row + 1, column + 1);

        if(numMines > 0)
        {
            tile.setText(Integer.toString(numMines));
        }
        else
        {
            tile.setText("");

            countMines(row - 1, column - 1);
            countMines(row - 1, column);
            countMines(row - 1, column + 1);
            countMines(row, column - 1);
            countMines(row, column + 1);
            countMines(row + 1, column - 1);
            countMines(row + 1, column);
            countMines(row + 1, column + 1);
        }
        if(tilesClicked == numRows * numColumns - minesCount)
        {
            gameOver = true;
            textLabel.setText("You won");

            setScore();
        }
    }

    void setScore()
    {
        String userName = JOptionPane.showInputDialog(frame, "Username:");
        int tilesClicked = this.tilesClicked;

        try
        {
            if(userName != null && userName != "")
            {
                FileWriter file = new FileWriter(new File("ScoreBoard.txt"), true);
                file.write(userName + "," + tilesClicked + "," + minesCount + "\n");
                file.close();
                file = null;
            }
        }
        catch(Exception err)
        {
            System.out.println("Error: " + err.getMessage());
        }

        String options[] = {"New Game", "Exit"};
        int choice = JOptionPane.showOptionDialog(frame, "Try again?", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if(choice == 0)
        {
            setBoard();
        }
        else
        {
            frame.dispose();
            System.exit(0);
        }
    }

    public static void main(String args[])
    {
        new Minesweeper();
    }
}