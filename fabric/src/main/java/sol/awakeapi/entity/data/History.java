package sol.awakeapi.entity.data;

import net.minecraft.nbt.NbtCompound;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record History(String biome, String weather, boolean isBaby, String flattenedStatusEffects, double healthPercentage, Interaction interaction, String dateTime) {

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("HistoryBiome", biome);
        nbt.putString("HistoryWeather", weather);
        nbt.putBoolean("HistoryIsBaby", isBaby);
        nbt.putString("HistoryStatusEffects", flattenedStatusEffects);
        nbt.putDouble("HistoryHealthPercentage", healthPercentage);
        if (interaction != null) {
            nbt.put("HistoryInteraction", interaction.toNbt());
        }
        nbt.putString("HistoryDateTime", dateTime);
        return nbt;
    }

    public static History fromNbt(NbtCompound nbt) {
        String biome = nbt.getString("HistoryBiome");
        String weather = nbt.getString("HistoryWeather");
        boolean isBaby = nbt.getBoolean("HistoryIsBaby");
        String flattenedStatusEffects = nbt.getString("HistoryStatusEffects");
        double healthPercentage = nbt.getDouble("HistoryHealthPercentage");
        Interaction interaction = null;
        if (nbt.contains("HistoryInteraction")) {
            interaction = Interaction.fromNbt(nbt.getCompound("HistoryInteraction"));
        }
        String dateTime = nbt.getString("HistoryDateTime");
        return new History(biome, weather, isBaby, flattenedStatusEffects, healthPercentage, interaction, dateTime);
    }

    @Override
    public String toString() {
        String interactionDetails = (interaction != null) ? "\nInteraction: " + interaction.getInteractionDetails() : "";
        return "History Item: {" +
                "\nBiome: " + biome +
                "\nWeather: " + weather +
                "\nisBaby: " + isBaby +
                "\nStatus Effects: " + flattenedStatusEffects +
                "\nHealth Percentage: " + healthPercentage +
                interactionDetails +
                "\nDateTime: " + dateTime;
    }

    public static String toShortString(List<History> histories) {
        if (histories == null || histories.isEmpty()) {
            return "No history available.";
        }

        StringBuilder summary = new StringBuilder();
        Map<String, List<History>> groupedByBiome = histories.stream().collect(Collectors.groupingBy(History::biome));

        for (Map.Entry<String, List<History>> entry : groupedByBiome.entrySet()) {
            String biome = entry.getKey();
            List<History> biomeHistories = entry.getValue();

            summary.append("Biome: ").append(biome).append("\n");

            // Baby status
            boolean wasBaby = biomeHistories.stream().anyMatch(History::isBaby);
            summary.append("You are ").append(wasBaby ? "a baby\n" : "not a baby\n");

            // Weather and time ranges
            Map<String, List<History>> groupedByWeather = biomeHistories.stream().collect(Collectors.groupingBy(History::weather));
            for (Map.Entry<String, List<History>> weatherEntry : groupedByWeather.entrySet()) {
                String weather = weatherEntry.getKey();
                List<History> weatherHistories = weatherEntry.getValue();
                String startTime = weatherHistories.get(0).dateTime;
                String endTime = weatherHistories.get(weatherHistories.size() - 1).dateTime;

                summary.append("The weather was ").append(weather)
                        .append(" between ").append(startTime).append(" and ").append(endTime).append("\n");
            }

            // Interactions and health changes
            Map<String, List<History>> groupedByInteraction = biomeHistories.stream()
                    .filter(history -> history.interaction != null)
                    .collect(Collectors.groupingBy(history -> history.interaction.getInteractionDetails() + " at " + history.dateTime));

            for (Map.Entry<String, List<History>> interactionEntry : groupedByInteraction.entrySet()) {
                String interactionDetails = interactionEntry.getKey();
                List<History> interactionHistories = interactionEntry.getValue();

                summary.append(interactionHistories.size()).append(" times: ").append(interactionDetails).append("\n");
            }

            for (History history : biomeHistories) {
                if (history.healthPercentage < 100.0) {
                    summary.append("Your health dropped to ").append(history.healthPercentage).append("% at ")
                            .append(history.dateTime).append("\n");
                }
            }
        }

        return summary.toString().trim();
    }
}
