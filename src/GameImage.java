import java.awt.Canvas;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

@SuppressWarnings("serial")
public class GameImage extends Canvas {

	Image image[] = null;
	private int CurrentFrame = 0;
	private int finalFrame = 0;

	// -------------------------------------------------
	public GameImage() {}

	public void InitAnimateFrame(int toThisFrame) {
		CurrentFrame = toThisFrame;
		// debug.print("GameImage: InitAnimateFrame - End.");
	}

	// -1: end of animate
	// +1: doing animate
	public int Animate() {
		if (CurrentFrame >= finalFrame)
			return -1;

		CurrentFrame++;

		// debug.print("GameImage: Animate - End.");
		return 1;
	}

	// For use in Block class, images are loaded and animation functions are set
	public void LoadImage(String filename, int divideCount, int divWidth) {
		Toolkit toolkit = getToolkit();
		Image tempImage = null;
		MediaTracker tracker = new MediaTracker(this);
		tempImage = toolkit.getImage(filename);
		tracker.addImage(tempImage, 100); // temp tracking to load this img

		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			debug.print("GameImage: LoadImage - InterruptedException!");
		}

		ImageProducer PRODUCER = tempImage.getSource();
		ImageFilter FILTER;
		image = new Image[divideCount];
		finalFrame = divideCount - 1;

		for (int col = 0; col < divideCount; col++) {
			FILTER = new CropImageFilter(col * divWidth, 0, tempImage.getWidth(null) / divideCount,
					tempImage.getHeight(null));
			debug.print("GameImage: LoadImage - width:%d, height:%d\n", tempImage.getWidth(null),
					tempImage.getHeight(null));
			image[col] = createImage(new FilteredImageSource(PRODUCER, FILTER));
			tracker.addImage(image[col], 0);
		}
		tracker.removeImage(tempImage, 100); // end of using of temp img...

		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			debug.print("GameImage: LoadImage - InterruptedException!");
		}
		debug.print("GameImage: LoadImage - End of code.");
	}

	public Image getImage() {
		// debug.print("GameImage: getImage - (%d/%d)\n", CurrentFrame, finalFrame);
		return image[CurrentFrame];
	}

	public int getWidth()
	{
		return image[CurrentFrame].getWidth(null);
	}
	public int getHeight()
	{
		return image[CurrentFrame].getHeight(null);
	}
}
