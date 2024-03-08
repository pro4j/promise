package com.tencent.wxpfc;

import android.util.Log;
import java.util.Arrays;
import java.util.Random;

public class TestUtil {
   /**
    * 生成随机数数据，此随机数保证了最小值一定与其他值间隔 {minGap} 避免过于接近的值导致单测用例不稳定
    *
    * @param count 随机值数量
    * @param min 最小值
    * @param max 最大值
    * @param minGap 最小值与其他值保持的绝对差距
    * @param shuffle 返回前对值进行随机排序
    * @return 随机化的数值
    */
   public static int[] getRandomNums(int count, int min, int max, int minGap, boolean shuffle) {
      int[] nums = new int[count];
      for (int i = 0; i < count; i++) {
         // 约束随机值范围在[min, max-gap)之间
         nums[i] = min + (int)(Math.random() * Math.max(max - min - minGap, 0)); // max-min-minGap有可能小于0
      }
      Arrays.sort(nums);
      Log.d("", "before:" + Arrays.toString(nums));
      // 时间最短的那个值与其他值保持至少minGap的差距
      int gapDiff = minGap - (nums[1] - nums[0]); // 可能为0
      for (int i=1; i<count; i++) {
         // 约束不得超过max最大值
         nums[i] = Math.min(max, nums[i] + gapDiff); // 为其他值加上调整值在数轴上右移
      }
      Log.d("", "after:" + Arrays.toString(nums));
      if (shuffle) { // 随机化输出
         Random random = new Random();
         for (int i = nums.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // 交换当前位置的元素和随机位置的元素
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
         }
         Log.d("", "shuffle:" + Arrays.toString(nums));
      }
      return nums;
   }
}
