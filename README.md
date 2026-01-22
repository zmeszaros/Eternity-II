# Eternity II - Puzzle Solver

## Áttekintés (Overview)

Az Eternity II egy összetett kirakós játék solver (megoldó) alkalmazás, amely egy backtracking algoritmust implementál Java nyelven. A program célja az Eternity II típusú puzzle-ok megoldása, ahol négyzet alakú lapkákat kell úgy elhelyezni egy táblán, hogy a szomszédos lapkák oldalai illeszkedjenek egymáshoz.
WIKI: https://en.wikipedia.org/wiki/Eternity_II_puzzle

## Mi az Eternity II?

Az Eternity II egy edge-matching (él-illesztéses) puzzle, ahol:
- Minden lapka négy oldallal rendelkezik
- Minden oldal egy színt vagy mintát tartalmaz
- A lapkákat úgy kell elhelyezni, hogy a szomszédos lapkák érintkező oldalai azonos színűek/mintájúak legyenek
- A tábla szélén levő lapkák külső oldalainak speciális "edge" (0) értékkel kell rendelkezniük

## Funkcionalitás

### Főbb Képességek

1. **Puzzle Betöltés**: Különböző méretű puzzle-ok beolvasása text fájlból (4x4, 6x6, 8x8, 16x16)
2. **Backtracking Algoritmus**: Intelligens visszalépéses algoritmus a megoldás megtalálásához
3. **Ellenőrzött Elemek Nyilvántartása**: Már kipróbált lapkák követése pozíciónként
4. **Fix Elemek Támogatása**: Előre rögzített lapkák támogatása (clue-k)
5. **Állapot Mentés/Betöltés**: A keresési állapot mentése és visszaállítása hosszú futások esetén
6. **Megoldások Mentése**: Talált megoldások mentése fájlba
7. **Statisztikák**: Futási idő, elhelyezett lapkák száma, teljesítmény metrikák

### Puzzle Típusok

A program három típusú lapkát különböztet meg:

1. **Corner (Sarok) Elemek**: Két él (side) értéke 0 (EDGE)
2. **Edge (Szél) Elemek**: Egy él értéke 0 (EDGE)
3. **Inner (Belső) Elemek**: Egyik él értéke sem 0

### Algoritmus Működése

#### 1. Inicializálás
- Puzzle elemek beolvasása fájlból
- Elemek típus szerinti osztályozása (sarok, szél, belső)
- Kapcsolatok (relations) létrehozása - mely lapkák illeszkedhetnek egymáshoz
- Táblázat inicializálása megadott mérettel

#### 2. Lapka Elhelyezés
```
Minden pozícióra (sor, oszlop):
  1. Lehetséges lapkák kiválasztása (potentials)
     - A pozíció típusának megfelelő lapka-készletből
     - Már elhelyezett szomszédokkal kompatibilis lapkák
  2. Lapka kiválasztása a lehetséges halmazból
  3. Lapka forgatása a megfelelő orientációba
  4. Ellenőrzés: illeszkedik-e a szomszédokhoz
  5. Ha illeszkedik: elhelyezés, továbblépés
  6. Ha nem illeszkedik: újabb próba más lapkával
  7. Ha nincs több lehetőség: backtrack (visszalépés)
```

#### 3. Backtracking
- Ha egy pozícióban nincs több kipróbálható lapka
- Visszalépés az előző pozícióra
- Az előző pozíció lapkájának eltávolítása
- Új lapka keresése az előző pozícióban
- Folyamat ismétlése sikeres elhelyezésig

#### 4. Optimalizálások
- **Relations (Kapcsolatok)**: Előre kiszámolt kompatibilis lapka párok
- **Potentials (Lehetőségek)**: Csak kompatibilis lapkák kipróbálása
- **Checked Elements**: Már kipróbált lapkák kihagyása
- **Same Elements**: Azonos lapkák kezelése (szimmetriák)

## Projekt Struktúra

### Fő Osztályok

#### `Eternity.java`
- Fő vezérlő osztály
- Tartalmazza a `main()` metódust
- Backtracking algoritmus implementáció
- Puzzle beolvasás és inicializálás
- Statisztikák gyűjtése és megjelenítése

#### `Puzzle.java`
- Egy puzzle lapkát reprezentál
- Tárolja a lapka 4 oldalának értékeit
- Implementálja a lapka forgatását (rotation)
- Előre kiszámítja az összes lehetséges forgatást

#### `Table.java`
- A játéktábla reprezentációja
- TableElem mátrix tárolása
- Lapkák elhelyezése és eltávolítása
- Illeszkedés ellenőrzése (fitElem)
- Potenciális lapkák kezelése pozíciónként

#### `TableElem.java`
- Egy tábla pozíció reprezentációja
- Tárolja az aktuálisan elhelyezett lapkát
- Tárolja a lehetséges lapkák halmazát
- Tárolja a már kipróbált lapkák halmazát
- Fix elem jelzés (clue-k esetén)

#### `Relations.java`
- Lapkák közötti kapcsolatok kezelése
- Minden lapka minden oldala esetén tárolja, mely más lapkák illeszkedhetnek hozzá
- HashMap<Integer, HashMap<Integer, Set>> struktúra
- Gyorsítja a kompatibilis lapkák keresését

#### `ProcessStatus.java`
- Aktuális keresési állapot mentése/betöltése
- Checkpoint funkció hosszú futások esetén
- Állapot szerializálása fájlba (.eii kiterjesztés)

#### Segédosztályok
- `MemoryQueue.java`: Üzenet várólista implementáció
- `PrintMessageQueue.java`: Konzolos kiírás kezelése külön szálban
- `WriteFileQueue.java`: Fájl írás kezelése külön szálban
- `FileToWrite.java`: Fájl írás adatok tárolása

## Használat

### Parancssori Argumentumok

```bash
java -jar Eternity.jar -r<sorok> -c<oszlopok> -p <puzzle_file> [-s <state_file>]
```

**Kötelező paraméterek:**
- `-r<szám>`: Sorok száma (pl. `-r8` = 8 sor)
- `-c<szám>`: Oszlopok száma (pl. `-c8` = 8 oszlop)
- `-p <fájl>`: Puzzle elemek fájlja

**Opcionális paraméterek:**
- `-s <fájl>`: Mentett állapot fájl betöltése

### Példák

```bash
# 4x4-es puzzle megoldása
java -jar Eternity.jar -r4 -c4 -p puzzles4x4.txt

# 8x8-as puzzle megoldása
java -jar Eternity.jar -r8 -c8 -p puzzles8x8.txt

# 16x16-os puzzle megoldása
java -jar Eternity.jar -r16 -c16 -p puzzles16x16.txt

# Mentett állapotból folytatás
java -jar Eternity.jar -r8 -c8 -p puzzles16x16.txt -s state_20170515165821992.eii
```

### Puzzle Fájl Formátum

A puzzle fájl CSV formátumú, ahol minden sor egy lapkát reprezentál:
```
top, right, bottom, left
```

Például:
```
0, 2, 1, 0     # Sarok elem (két 0-s oldal)
0, 0, 2, 1     # Sarok elem
4, 5, 4, 7     # Belső elem (nincs 0-s oldal)
0, 1, 5, 3     # Szél elem (egy 0-s oldal)
```

## Kimenet

### Konzol Kimenet
- Aktuális legjobb mélység (hány lapka helyezhető el)
- Tábla állapot ASCII formában
- Futási statisztikák:
  - Megoldások száma
  - Maximális mélység
  - Eltelt idő
  - Elhelyezett lapkák száma
  - Teljesítmény (millió elhelyezés/sec)

### Fájl Kimenet
- `solutions.txt`: Teljes megoldások
- `clueSolutions.txt`: Részleges megoldások (clue-kkal)
- `state_*.eii`: Automatikus állapot mentések (óránként)

### Tábla Formátum Példa
```
+-----+-----+-----+-----+
|   1 |   2 |   3 |   4 |
+-----+-----+-----+-----+
|   5 |   6 |   7 |   8 |
+-----+-----+-----+-----+
|   9 |  10 |  11 |  12 |
+-----+-----+-----+-----+
|  13 |  14 |  15 |  16 |
+-----+-----+-----+-----+
```

## Technikai Részletek

### Teljesítmény Optimalizálások

1. **Előszámított Forgatások**: Minden lapka összes forgatása előre kiszámolva
2. **Kapcsolati Tábla**: Gyors kompatibilitás ellenőrzés
3. **Potentials Szűkítés**: Csak releváns lapkák vizsgálata
4. **Same Elements**: Azonos lapkák egyszer kipróbálása
5. **Multithreading**: Külön szálak a fájl I/O és konzol kimenethez

### Memória Kezelés
- Klónozott tábla állapotok mentéskor
- Explicit garbage collection mentés után
- Hatékony Set és HashMap használat

### Állapot Mentés
- Automatikus mentés óránként (Timer)
- Mentett adatok:
  - Aktuális pozíció (sor, oszlop)
  - Tábla állapot (elhelyezett lapkák)
  - Fix elemek jelzései
  - Potentials minden pozícióhoz
  - Checked elemek minden pozícióhoz

## Fejlesztés

### Fordítás

```bash
javac -cp lib/commons-cli-1.2.jar -d bin src/*.java
```

### JAR Készítés

```bash
jar cfm Eternity.jar manifest.txt -C bin eternity
```

### Függőségek

- **Apache Commons CLI 1.2**: Parancssori argumentum kezelés
- Java SE 6 vagy újabb

## Licenc

MIT License - Lásd LICENSE fájl a részletekért

## Megjegyzések

- Az Eternity II puzzle egy NP-teljes probléma
- A 16x16-os eredeti puzzle még mindig megoldatlan
- A program backtracking jellege miatt exponenciális időbonyolultságú
- Hosszú futási idők várhatók nagyobb méretű puzzle-ok esetén
- Állapot mentés/betöltés lehetővé teszi a hosszú keresések megszakítását és folytatását

## Jelenlegi Limitációk

1. Egyetlen szálú keresés (csak I/O multithreaded)
2. Determinisztikus elem választás (nincs randomizálás)
3. Korlátozott heurisztikák
4. Nagy memóriahasználat komplex állapotnál

## Továbbfejlesztési Lehetőségek

- Párhuzamos keresés több szálban
- Fejlettebb heurisztikák (pl. Minimum Remaining Values)
- Constraint Propagation technikák
- Genetikus algoritmus vagy más metaheurisztikák
- GUI felület
- Vizualizáció a keresési folyamatról
