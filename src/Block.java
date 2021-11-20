import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.util.Hashtable;
import java.util.LinkedList;

public class Block
{
	// fields
	private int posx;
	private int posy;
	private int width;
	private int height;
	private int marginLeft;
	private int marginRight;
	private int marginTop;
	private int marginBottom;
	/*
	 * static���� �����Ͽ� �ߺ� load�� ���� (�������� Block�� �ϳ��� gameimage_table�� �����ϰ� �ȴ�)
	 */
	private static Hashtable<String, GameImage> gameimage_table;
	/*
	 * ����� block�� image ���� �̸���.
	 * 'select'�� ������ png�� ������ border�� ��Ÿ���� ���� block���� �ε��.
	 */
	private static String[] gameimagelist = { "block_1", "block_2", "block_3", "block_4", "select" };
	public int BlockKinds; // currently, 0 - 3. defined in MainFrame.blockkindsCount
	public GameImage blockimg;
	public static GameImage SelectImage;
	public enum BlockState
	{
		dead, alive, selected
	}
	public BlockState blockstate;

	public static LinkedList<Block> selectedlist;

	// methods
	// init
	/**
	 * 04/13:
	 * ���� margin �Է��� �߰�.
	 */
	protected Block(int posx, int posy, int width, int height, int margin_top, int margin_right,
			int margin_bottom, int margin_left, int BlockKinds)
	{
		this.posx = posx;
		this.posy = posy;
		this.width = width;
		this.height = height;
		this.marginLeft = margin_left;
		this.marginRight = margin_right;
		this.marginTop = margin_top;
		this.marginBottom = margin_bottom;
		this.BlockKinds = BlockKinds;
		blockstate = BlockState.alive;
		if (gameimage_table == null)
			gameimage_table = new Hashtable<String, GameImage>();
		if (selectedlist == null)
			selectedlist = new LinkedList<Block>();
	}
	// methods
	public void LoadContents(String contentPath)
	{
		if (gameimage_table == null)
			throw new RuntimeException("something's wrong during LoadContents");
		if (gameimage_table.size() == 0)
		{
			// start loading
			GameImage imgloaded = null;
			for (String loadinglistitem : gameimagelist)
			{
				imgloaded = new GameImage();
				imgloaded.LoadImage(contentPath + loadinglistitem + ".png", 1, 0);
				gameimage_table.put(loadinglistitem, imgloaded);
			}
			SelectImage = gameimage_table.get("select");
		}
		blockimg = gameimage_table.get(gameimagelist[BlockKinds]);
	}
	public void UpdateGameProc(int mousex, int mousey, boolean isclicked)
	{
		// game logic begins
		if (this.blockstate != BlockState.dead && isclicked
				&& Block.isMouseOveredBlock(this, mousex, mousey))
		{
			this.blockstate = BlockState.selected;
			selectedlist.add(this);
		}

		// debug.print(""+this.blockstate);
		blockimg = gameimage_table.get(gameimagelist[BlockKinds]);
	}
	public void paint(Graphics g, ImageObserver imgob)
	{
		// drawing
		switch (blockstate)
		{
		case alive:
			g.drawImage(blockimg.getImage(), posx, posy, imgob);
			break;
		case selected:
			g.drawImage(blockimg.getImage(), posx, posy, imgob);
			g.drawImage(SelectImage.getImage(), posx, posy, imgob);
			break;
		case dead:
			break;
		}
	}
	// static methods
	public static boolean isMouseOveredBlock(Block block, int mousex, int mousey)
	{
		/*
		 * if ((block.posx < mousex) && (mousex < block.posx + block.width))
		 * if ((block.posy < mousey) && (mousey < block.posy + block.height))
		 * return true;
		 */
		/**
		 * 04/13:
		 * margin�� ���� ���콺 Ŭ�� ������ ����
		 */
		if ((block.posx + block.marginLeft < mousex)
				&& (mousex < block.posx + block.width - block.marginRight))
			if ((block.posy + block.marginTop < mousey)
					&& (mousey < block.posy + block.height - block.marginBottom))
				return true;
		return false;
	}
}
