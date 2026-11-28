package ru.atl.skills.client;
final class EnchantDescriptions {
    private EnchantDescriptions() {}
    static String description(String id) {
        return switch (id) {
            case "minecraft:protection" -> "Уменьшает большинство видов урона.";
            case "minecraft:fire_protection" -> "Уменьшает урон от огня и лавы.";
            case "minecraft:blast_protection" -> "Уменьшает урон от взрывов.";
            case "minecraft:projectile_protection" -> "Уменьшает урон от снарядов.";
            case "minecraft:feather_falling" -> "Уменьшает урон от падения.";
            case "minecraft:respiration" -> "Позволяет дольше дышать под водой.";
            case "minecraft:aqua_affinity" -> "Ускоряет добычу блоков под водой.";
            case "minecraft:thorns" -> "Наносит урон атакующим врагам.";
            case "minecraft:depth_strider" -> "Ускоряет передвижение под водой.";
            case "minecraft:frost_walker" -> "Замораживает воду под ногами.";
            case "minecraft:soul_speed" -> "Ускоряет движение по песку и почве душ.";
            case "minecraft:swift_sneak" -> "Ускоряет передвижение крадучись.";
            case "minecraft:sharpness" -> "Увеличивает урон оружия.";
            case "minecraft:smite" -> "Увеличивает урон по нежити.";
            case "minecraft:bane_of_arthropods" -> "Усиливает урон по членистоногим.";
            case "minecraft:knockback" -> "Усиливает отбрасывание цели.";
            case "minecraft:fire_aspect" -> "Поджигает противника при ударе.";
            case "minecraft:looting" -> "Увеличивает добычу с мобов.";
            case "minecraft:sweeping_edge" -> "Усиливает размашистую атаку.";
            case "minecraft:efficiency" -> "Ускоряет добычу блоков инструментом.";
            case "minecraft:silk_touch" -> "Позволяет добывать блоки целиком.";
            case "minecraft:unbreaking" -> "Уменьшает расход прочности.";
            case "minecraft:fortune" -> "Увеличивает добычу некоторых блоков.";
            case "minecraft:power" -> "Увеличивает урон стрел из лука.";
            case "minecraft:punch" -> "Усиливает отбрасывание стрелой.";
            case "minecraft:flame" -> "Поджигает выпущенные стрелы.";
            case "minecraft:infinity" -> "Позволяет не расходовать обычные стрелы.";
            case "minecraft:luck_of_the_sea" -> "Увеличивает шанс ценной добычи на рыбалке.";
            case "minecraft:lure" -> "Ускоряет поклёвку.";
            case "minecraft:loyalty" -> "Возвращает брошенный трезубец.";
            case "minecraft:impaling" -> "Усиливает урон трезубца.";
            case "minecraft:riptide" -> "Рывок с трезубцем в воде или под дождём.";
            case "minecraft:channeling" -> "Призывает молнию в грозу.";
            case "minecraft:multishot" -> "Арбалет выпускает несколько снарядов.";
            case "minecraft:quick_charge" -> "Ускоряет перезарядку арбалета.";
            case "minecraft:piercing" -> "Снаряд арбалета пронзает цели.";
            case "minecraft:density" -> "Усиливает удар булавы при падении.";
            case "minecraft:breach" -> "Уменьшает защиту брони цели от булавы.";
            case "minecraft:wind_burst" -> "Отбрасывает владельца вверх после удара булавой.";
            case "minecraft:lunge" -> "Усиливает рывок с копьём.";
            case "minecraft:mending" -> "Восстанавливает прочность за опыт.";
            default -> "Улучшает свойства предмета.";
        };
    }
}
