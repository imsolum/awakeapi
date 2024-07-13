package sol.awakeapi.entity.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

public class Emotion {
    private static final Random RANDOM = new Random();

    private static final Map<String, List<String>> groups = Map.of(
            "Mood", Arrays.asList("angsty", "moody", "sad", "carefree", "joyful", "happy", "easy-going", "grumpy", "triggered"),
            "Truthfulness", Arrays.asList("liar", "honest", "candid", "diplomatic", "evasive", "deceptive"),
            "EmotionalReaction", Arrays.asList("emotional", "stoic", "composed", "reactive", "sensitive", "melancholic"),
            "Flexibility", Arrays.asList("impressionable", "rigid", "unimpressionable", "adaptable", "unyielding", "conservative", "innovative"),
            "Patience", Arrays.asList("impatient", "patient", "enduring", "tolerant", "eager"),
            "Confidence", Arrays.asList("assertive", "bold", "self-assured", "timid", "hesitant", "self-doubting"),
            "Responsibility", Arrays.asList("responsible", "irresponsible")
    );

    public static List<String> getRandomEmotions(int numberOfGroups) {
        List<String> selectedEmotions = new ArrayList<>();
        List<String> keys = new ArrayList<>(groups.keySet());
        Collections.shuffle(keys);

        keys.stream().limit(numberOfGroups).forEach(key -> {
            List<String> emotions = groups.get(key);
            String selectedEmotion = emotions.get(RANDOM.nextInt(emotions.size()));
            selectedEmotions.add(selectedEmotion);
        });

        return selectedEmotions;
    }

    public static List<String> getRandomEmotions(int minGroups, int maxGroups) {
        if (minGroups > maxGroups) {
            int temp = minGroups;
            minGroups = maxGroups;
            maxGroups = temp;
        }

        int numberOfGroups = RANDOM.nextInt(maxGroups - minGroups + 1) + minGroups;
        List<String> selectedEmotions = new ArrayList<>();
        List<String> keys = new ArrayList<>(groups.keySet());
        Collections.shuffle(keys);

        keys.stream().limit(numberOfGroups).forEach(key -> {
            List<String> emotions = groups.get(key);
            String selectedEmotion = emotions.get(RANDOM.nextInt(emotions.size()));
            selectedEmotions.add(selectedEmotion);
        });

        return selectedEmotions;
    }

    public static NbtCompound toNbt(List<String> emotions) {
        NbtCompound compound = new NbtCompound();
        NbtList nbtList = new NbtList();
        for (String emotion : emotions) {
            nbtList.add(NbtString.of(emotion)); // Add NbtString objects to the list
        }
        compound.put("Emotions", nbtList);
        return compound;
    }

    public static List<String> fromNbt(NbtCompound compound) {
        NbtList nbtList = compound.getList("Emotions", 8); // 8 is the ID for String in NBT
        List<String> emotions = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            emotions.add(nbtList.getString(i));
        }
        return emotions;
    }
}
