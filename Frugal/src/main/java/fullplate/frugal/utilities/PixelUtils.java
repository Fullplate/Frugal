package fullplate.frugal.utilities;

import android.content.Context;

public class PixelUtils
{
    public static int dpToPixels(final Context context, final int dp)
    {
        if (dp > 0)
        {
            return Math.max(1, (int) (dp * context.getResources().getDisplayMetrics().density));
        }
        else if (dp < 0)
        {
            return Math.min(-1, (int) (dp * context.getResources().getDisplayMetrics().density));
        }
        else // dp == 0
        {
            return 0;
        }
    }

}

