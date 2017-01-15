package onitama.tests;

import onitama.ai.Searcher;
import onitama.model.Card;
import onitama.model.CardState;

/*
Depth 1, board 0, cards 0: score = 1, states = 13, time = 0 ms
Depth 1, board 0, cards 1: score = 0, states = 12, time = 0 ms
Depth 1, board 1, cards 0: score = 10, states = 19, time = 0 ms
Depth 1, board 1, cards 1: score = 10, states = 17, time = 0 ms
Depth 1, board 2, cards 0: score = 2, states = 11, time = 0 ms
Depth 1, board 2, cards 1: score = 2, states = 17, time = 0 ms

Depth 2, board 0, cards 0: score = -1, states = 88, time = 0 ms
Depth 2, board 0, cards 1: score = -1, states = 113, time = 0 ms
Depth 2, board 1, cards 0: score = 8, states = 74, time = 0 ms
Depth 2, board 1, cards 1: score = 9, states = 130, time = 0 ms
Depth 2, board 2, cards 0: score = 0, states = 66, time = 0 ms
Depth 2, board 2, cards 1: score = 0, states = 103, time = 0 ms

Depth 3, board 0, cards 0: score = 0, states = 463, time = 0 ms
Depth 3, board 0, cards 1: score = 1, states = 406, time = 0 ms
Depth 3, board 1, cards 0: score = 12, states = 727, time = 0 ms
Depth 3, board 1, cards 1: score = 12, states = 937, time = 0 ms
Depth 3, board 2, cards 0: score = 3, states = 444, time = 0 ms
Depth 3, board 2, cards 1: score = 3, states = 793, time = 0 ms

Depth 4, board 0, cards 0: score = -3, states = 3474, time = 0 ms
Depth 4, board 0, cards 1: score = -1, states = 2213, time = 0 ms
Depth 4, board 1, cards 0: score = 8, states = 3672, time = 4 ms
Depth 4, board 1, cards 1: score = 8, states = 4870, time = 0 ms
Depth 4, board 2, cards 0: score = 0, states = 2071, time = 0 ms
Depth 4, board 2, cards 1: score = -1, states = 4509, time = 0 ms

Depth 5, board 0, cards 0: score = 0, states = 11750, time = 0 ms
Depth 5, board 0, cards 1: score = 1, states = 6899, time = 0 ms
Depth 5, board 1, cards 0: score = 12, states = 26892, time = 8 ms
Depth 5, board 1, cards 1: score = 12, states = 33717, time = 8 ms
Depth 5, board 2, cards 0: score = 10, states = 10535, time = 0 ms
Depth 5, board 2, cards 1: score = 3, states = 20170, time = 4 ms

Depth 6, board 0, cards 0: score = -3, states = 56615, time = 8 ms
Depth 6, board 0, cards 1: score = -1, states = 55228, time = 4 ms
Depth 6, board 1, cards 0: score = 8, states = 132802, time = 16 ms
Depth 6, board 1, cards 1: score = 10, states = 140766, time = 24 ms
Depth 6, board 2, cards 0: score = -2, states = 53701, time = 8 ms
Depth 6, board 2, cards 1: score = -1, states = 120796, time = 12 ms

Depth 7, board 0, cards 0: score = 1, states = 295418, time = 36 ms
Depth 7, board 0, cards 1: score = 1, states = 212303, time = 24 ms
Depth 7, board 1, cards 0: score = 12, states = 705307, time = 84 ms
Depth 7, board 1, cards 1: score = 12, states = 844101, time = 96 ms
Depth 7, board 2, cards 0: score = 12, states = 277567, time = 32 ms
Depth 7, board 2, cards 1: score = 8, states = 816385, time = 80 ms

Depth 8, board 0, cards 0: score = -2, states = 1098990, time = 164 ms
Depth 8, board 0, cards 1: score = -1, states = 1166546, time = 116 ms
Depth 8, board 1, cards 0: score = 9, states = 2818009, time = 312 ms
Depth 8, board 1, cards 1: score = 10, states = 4621091, time = 468 ms
Depth 8, board 2, cards 0: score = 0, states = 1250566, time = 156 ms
Depth 8, board 2, cards 1: score = -1, states = 2503826, time = 272 ms

Depth 9, board 0, cards 0: score = 1, states = 5813642, time = 752 ms
Depth 9, board 0, cards 1: score = 1, states = 4112814, time = 508 ms
Depth 9, board 1, cards 0: score = 18, states = 16320028, time = 1960 ms
Depth 9, board 1, cards 1: score = 12, states = 17089929, time = 2013 ms
Depth 9, board 2, cards 0: score = 12, states = 8064697, time = 988 ms
Depth 9, board 2, cards 1: score = 4, states = 10845691, time = 1212 ms

Depth 10, board 0, cards 0: score = -2, states = 25879303, time = 3176 ms
Depth 10, board 0, cards 1: score = -2, states = 29049082, time = 2928 ms
Depth 10, board 1, cards 0: score = 9, states = 72405607, time = 8352 ms
Depth 10, board 1, cards 1: score = 10, states = 99839951, time = 10496 ms
Depth 10, board 2, cards 0: score = 2, states = 37997058, time = 4476 ms
Depth 10, board 2, cards 1: score = -1, states = 57651099, time = 6273 ms

Depth 11, board 0, cards 0: score = 1, states = 134173830, time = 17038 ms
Depth 11, board 0, cards 1: score = 1, states = 47931818, time = 6084 ms
Depth 11, board 1, cards 0: score = 34, states = 307066415, time = 38655 ms
Depth 11, board 1, cards 1: score = 12, states = 379325728, time = 48431 ms
Depth 11, board 2, cards 0: score = 18, states = 197296635, time = 22954 ms
Depth 11, board 2, cards 1: score = 2, states = 284573354, time = 32487 ms

Total states: 1752741903
Total time: 224735 ms

Iterative deepening:

Depth 1, board 0, cards 0: score = 1, states = 13, time = 4 ms
Depth 1, board 0, cards 1: score = 0, states = 12, time = 0 ms
Depth 1, board 1, cards 0: score = 10, states = 19, time = 4 ms
Depth 1, board 1, cards 1: score = 10, states = 17, time = 0 ms
Depth 1, board 2, cards 0: score = 2, states = 11, time = 0 ms
Depth 1, board 2, cards 1: score = 2, states = 17, time = 0 ms

Depth 2, board 0, cards 0: score = -1, states = 101, time = 0 ms
Depth 2, board 0, cards 1: score = -1, states = 125, time = 0 ms
Depth 2, board 1, cards 0: score = 8, states = 93, time = 0 ms
Depth 2, board 1, cards 1: score = 9, states = 147, time = 0 ms
Depth 2, board 2, cards 0: score = 0, states = 77, time = 0 ms
Depth 2, board 2, cards 1: score = 0, states = 120, time = 0 ms

Depth 3, board 0, cards 0: score = 0, states = 564, time = 0 ms
Depth 3, board 0, cards 1: score = 1, states = 531, time = 0 ms
Depth 3, board 1, cards 0: score = 12, states = 820, time = 4 ms
Depth 3, board 1, cards 1: score = 12, states = 1084, time = 0 ms
Depth 3, board 2, cards 0: score = 3, states = 521, time = 0 ms
Depth 3, board 2, cards 1: score = 3, states = 913, time = 0 ms

Depth 4, board 0, cards 0: score = -3, states = 4038, time = 4 ms
Depth 4, board 0, cards 1: score = -1, states = 2744, time = 0 ms
Depth 4, board 1, cards 0: score = 8, states = 4492, time = 0 ms
Depth 4, board 1, cards 1: score = 8, states = 5954, time = 0 ms
Depth 4, board 2, cards 0: score = 0, states = 2592, time = 0 ms
Depth 4, board 2, cards 1: score = -1, states = 5422, time = 0 ms

Depth 5, board 0, cards 0: score = 0, states = 15788, time = 4 ms
Depth 5, board 0, cards 1: score = 1, states = 9643, time = 4 ms
Depth 5, board 1, cards 0: score = 12, states = 31384, time = 12 ms
Depth 5, board 1, cards 1: score = 12, states = 39671, time = 4 ms
Depth 5, board 2, cards 0: score = 10, states = 13127, time = 4 ms
Depth 5, board 2, cards 1: score = 3, states = 25592, time = 4 ms

Depth 6, board 0, cards 0: score = -3, states = 72403, time = 12 ms
Depth 6, board 0, cards 1: score = -1, states = 64871, time = 4 ms
Depth 6, board 1, cards 0: score = 8, states = 164186, time = 16 ms
Depth 6, board 1, cards 1: score = 10, states = 180437, time = 16 ms
Depth 6, board 2, cards 0: score = -2, states = 66828, time = 8 ms
Depth 6, board 2, cards 1: score = -1, states = 146388, time = 16 ms

Depth 7, board 0, cards 0: score = 1, states = 367816, time = 44 ms
Depth 7, board 0, cards 1: score = 1, states = 276774, time = 28 ms
Depth 7, board 1, cards 0: score = 12, states = 869466, time = 100 ms
Depth 7, board 1, cards 1: score = 12, states = 1025765, time = 116 ms
Depth 7, board 2, cards 0: score = 12, states = 344391, time = 40 ms
Depth 7, board 2, cards 1: score = 8, states = 962797, time = 112 ms

Depth 8, board 0, cards 0: score = -2, states = 1466596, time = 164 ms
Depth 8, board 0, cards 1: score = -1, states = 1440113, time = 140 ms
Depth 8, board 1, cards 0: score = 9, states = 3685912, time = 396 ms
Depth 8, board 1, cards 1: score = 10, states = 5629870, time = 597 ms
Depth 8, board 2, cards 0: score = 0, states = 1594760, time = 196 ms
Depth 8, board 2, cards 1: score = -1, states = 3460247, time = 360 ms

Depth 9, board 0, cards 0: score = 1, states = 7262209, time = 928 ms
Depth 9, board 0, cards 1: score = 1, states = 5524801, time = 640 ms
Depth 9, board 1, cards 0: score = 18, states = 19991268, time = 2352 ms
Depth 9, board 1, cards 1: score = 12, states = 22684423, time = 2580 ms
Depth 9, board 2, cards 0: score = 12, states = 9654799, time = 1100 ms
Depth 9, board 2, cards 1: score = 4, states = 14352014, time = 1649 ms

Depth 10, board 0, cards 0: score = -2, states = 33141025, time = 3901 ms
Depth 10, board 0, cards 1: score = -2, states = 34030875, time = 3648 ms
Depth 10, board 1, cards 0: score = 9, states = 92245346, time = 10073 ms
Depth 10, board 1, cards 1: score = 10, states = 121171030, time = 12317 ms
Depth 10, board 2, cards 0: score = 2, states = 47648535, time = 5392 ms
Depth 10, board 2, cards 1: score = -1, states = 71576758, time = 7708 ms

Depth 11, board 0, cards 0: score = 1, states = 166940916, time = 20478 ms
Depth 11, board 0, cards 1: score = 1, states = 80953637, time = 9241 ms
Depth 11, board 1, cards 0: score = 34, states = 398085912, time = 46931 ms
Depth 11, board 1, cards 1: score = 12, states = 491867338, time = 59664 ms
Depth 11, board 2, cards 0: score = 18, states = 243982081, time = 27178 ms
Depth 11, board 2, cards 1: score = 2, states = 342001546, time = 37603 ms

Total states: 2225099765
Total time: 270617 ms


Killer moves:

Depth 1, board 0, cards 0: score = 1, states = 13, time = 4 ms
Depth 1, board 0, cards 1: score = 0, states = 12, time = 4 ms
Depth 1, board 1, cards 0: score = 10, states = 19, time = 4 ms
Depth 1, board 1, cards 1: score = 10, states = 17, time = 0 ms
Depth 1, board 2, cards 0: score = 2, states = 11, time = 0 ms
Depth 1, board 2, cards 1: score = 2, states = 17, time = 0 ms

Depth 2, board 0, cards 0: score = -1, states = 96, time = 0 ms
Depth 2, board 0, cards 1: score = -1, states = 125, time = 4 ms
Depth 2, board 1, cards 0: score = 8, states = 67, time = 4 ms
Depth 2, board 1, cards 1: score = 9, states = 71, time = 0 ms
Depth 2, board 2, cards 0: score = 0, states = 41, time = 0 ms
Depth 2, board 2, cards 1: score = 0, states = 75, time = 0 ms

Depth 3, board 0, cards 0: score = 0, states = 455, time = 4 ms
Depth 3, board 0, cards 1: score = 1, states = 368, time = 0 ms
Depth 3, board 1, cards 0: score = 12, states = 480, time = 0 ms
Depth 3, board 1, cards 1: score = 12, states = 519, time = 0 ms
Depth 3, board 2, cards 0: score = 3, states = 419, time = 0 ms
Depth 3, board 2, cards 1: score = 3, states = 791, time = 4 ms

Depth 4, board 0, cards 0: score = -3, states = 3065, time = 0 ms
Depth 4, board 0, cards 1: score = -1, states = 2610, time = 0 ms
Depth 4, board 1, cards 0: score = 8, states = 2238, time = 4 ms
Depth 4, board 1, cards 1: score = 8, states = 1970, time = 0 ms
Depth 4, board 2, cards 0: score = 0, states = 2168, time = 0 ms
Depth 4, board 2, cards 1: score = -1, states = 3365, time = 0 ms

Depth 5, board 0, cards 0: score = 0, states = 9868, time = 4 ms
Depth 5, board 0, cards 1: score = 1, states = 7829, time = 0 ms
Depth 5, board 1, cards 0: score = 12, states = 14367, time = 4 ms
Depth 5, board 1, cards 1: score = 12, states = 14479, time = 0 ms
Depth 5, board 2, cards 0: score = 10, states = 9420, time = 0 ms
Depth 5, board 2, cards 1: score = 3, states = 15466, time = 4 ms

Depth 6, board 0, cards 0: score = -3, states = 24425, time = 4 ms
Depth 6, board 0, cards 1: score = -1, states = 36449, time = 8 ms
Depth 6, board 1, cards 0: score = 8, states = 29715, time = 4 ms
Depth 6, board 1, cards 1: score = 10, states = 29968, time = 4 ms
Depth 6, board 2, cards 0: score = -2, states = 37501, time = 8 ms
Depth 6, board 2, cards 1: score = -1, states = 68699, time = 8 ms

Depth 7, board 0, cards 0: score = 1, states = 165341, time = 20 ms
Depth 7, board 0, cards 1: score = 1, states = 144648, time = 16 ms
Depth 7, board 1, cards 0: score = 12, states = 197900, time = 20 ms
Depth 7, board 1, cards 1: score = 12, states = 173841, time = 24 ms
Depth 7, board 2, cards 0: score = 12, states = 197214, time = 28 ms
Depth 7, board 2, cards 1: score = 8, states = 516933, time = 52 ms

Depth 8, board 0, cards 0: score = -2, states = 507328, time = 72 ms
Depth 8, board 0, cards 1: score = -1, states = 562464, time = 92 ms
Depth 8, board 1, cards 0: score = 9, states = 316265, time = 44 ms
Depth 8, board 1, cards 1: score = 10, states = 474115, time = 76 ms
Depth 8, board 2, cards 0: score = 0, states = 310918, time = 40 ms
Depth 8, board 2, cards 1: score = -1, states = 1365961, time = 152 ms

Depth 9, board 0, cards 0: score = 1, states = 2826552, time = 392 ms
Depth 9, board 0, cards 1: score = 1, states = 3572205, time = 448 ms
Depth 9, board 1, cards 0: score = 18, states = 2082122, time = 224 ms
Depth 9, board 1, cards 1: score = 12, states = 2167846, time = 280 ms
Depth 9, board 2, cards 0: score = 12, states = 1236610, time = 140 ms
Depth 9, board 2, cards 1: score = 4, states = 4924718, time = 536 ms

Depth 10, board 0, cards 0: score = -2, states = 7542443, time = 904 ms
Depth 10, board 0, cards 1: score = -2, states = 26977332, time = 2792 ms
Depth 10, board 1, cards 0: score = 9, states = 3673088, time = 476 ms
Depth 10, board 1, cards 1: score = 10, states = 5639948, time = 732 ms
Depth 10, board 2, cards 0: score = 2, states = 2043739, time = 260 ms
Depth 10, board 2, cards 1: score = -1, states = 17621728, time = 1976 ms

Depth 11, board 0, cards 0: score = 1, states = 25428439, time = 3104 ms
Depth 11, board 0, cards 1: score = 1, states = 75318260, time = 8745 ms
Depth 11, board 1, cards 0: score = 34, states = 26521628, time = 2913 ms
Depth 11, board 1, cards 1: score = 12, states = 25216827, time = 2852 ms
Depth 11, board 2, cards 0: score = 18, states = 23756861, time = 2616 ms
Depth 11, board 2, cards 1: score = 2, states = 73910325, time = 8225 ms

Total states: 335710797
Total time: 54994 ms

(Without TT: 532581558 / 74428 ms)


Better static evaluation:

Depth 1, board 0, cards 0: score = 2, states = 13, time = 6 ms
Depth 1, board 0, cards 1: score = 2, states = 12, time = 1 ms
Depth 1, board 1, cards 0: score = 25, states = 19, time = 1 ms
Depth 1, board 1, cards 1: score = 24, states = 17, time = 1 ms
Depth 1, board 2, cards 0: score = 2, states = 11, time = 1 ms
Depth 1, board 2, cards 1: score = 2, states = 17, time = 0 ms

Depth 2, board 0, cards 0: score = 0, states = 48, time = 1 ms
Depth 2, board 0, cards 1: score = 0, states = 48, time = 0 ms
Depth 2, board 1, cards 0: score = 23, states = 65, time = 1 ms
Depth 2, board 1, cards 1: score = 22, states = 64, time = 1 ms
Depth 2, board 2, cards 0: score = 0, states = 41, time = 1 ms
Depth 2, board 2, cards 1: score = 0, states = 65, time = 0 ms

Depth 3, board 0, cards 0: score = 3, states = 358, time = 1 ms
Depth 3, board 0, cards 1: score = 2, states = 255, time = 1 ms
Depth 3, board 1, cards 0: score = 26, states = 498, time = 1 ms
Depth 3, board 1, cards 1: score = 24, states = 430, time = 1 ms
Depth 3, board 2, cards 0: score = 3, states = 508, time = 1 ms
Depth 3, board 2, cards 1: score = 4, states = 721, time = 1 ms

Depth 4, board 0, cards 0: score = -1, states = 1960, time = 2 ms
Depth 4, board 0, cards 1: score = 0, states = 1159, time = 1 ms
Depth 4, board 1, cards 0: score = 22, states = 1463, time = 2 ms
Depth 4, board 1, cards 1: score = 20, states = 1446, time = 1 ms
Depth 4, board 2, cards 0: score = -2, states = 2067, time = 2 ms
Depth 4, board 2, cards 1: score = 0, states = 1686, time = 2 ms

Depth 5, board 0, cards 0: score = 3, states = 10309, time = 6 ms
Depth 5, board 0, cards 1: score = 2, states = 4560, time = 1 ms
Depth 5, board 1, cards 0: score = 27, states = 15755, time = 8 ms
Depth 5, board 1, cards 1: score = 23, states = 14731, time = 3 ms
Depth 5, board 2, cards 0: score = 21, states = 9710, time = 6 ms
Depth 5, board 2, cards 1: score = 4, states = 13675, time = 3 ms

Depth 6, board 0, cards 0: score = -3, states = 46533, time = 12 ms
Depth 6, board 0, cards 1: score = 0, states = 19680, time = 5 ms
Depth 6, board 1, cards 0: score = 19, states = 41839, time = 9 ms
Depth 6, board 1, cards 1: score = 20, states = 27557, time = 6 ms
Depth 6, board 2, cards 0: score = 0, states = 22647, time = 7 ms
Depth 6, board 2, cards 1: score = 0, states = 37358, time = 8 ms

Depth 7, board 0, cards 0: score = 4, states = 253869, time = 53 ms
Depth 7, board 0, cards 1: score = 2, states = 156862, time = 28 ms
Depth 7, board 1, cards 0: score = 26, states = 236042, time = 44 ms
Depth 7, board 1, cards 1: score = 22, states = 216808, time = 41 ms
Depth 7, board 2, cards 0: score = 23, states = 126000, time = 24 ms
Depth 7, board 2, cards 1: score = 20, states = 493815, time = 93 ms

Depth 8, board 0, cards 0: score = -3, states = 598488, time = 126 ms
Depth 8, board 0, cards 1: score = -2, states = 443713, time = 100 ms
Depth 8, board 1, cards 0: score = 21, states = 347776, time = 68 ms
Depth 8, board 1, cards 1: score = 20, states = 484900, time = 101 ms
Depth 8, board 2, cards 0: score = -1, states = 380460, time = 86 ms
Depth 8, board 2, cards 1: score = -2, states = 1007426, time = 184 ms

Depth 9, board 0, cards 0: score = 4, states = 1814812, time = 366 ms
Depth 9, board 0, cards 1: score = 3, states = 1540613, time = 298 ms
Depth 9, board 1, cards 0: score = 38, states = 2737710, time = 493 ms
Depth 9, board 1, cards 1: score = 24, states = 2289078, time = 413 ms
Depth 9, board 2, cards 0: score = 24, states = 3199827, time = 620 ms
Depth 9, board 2, cards 1: score = 4, states = 4016539, time = 736 ms

Depth 10, board 0, cards 0: score = -2, states = 6744802, time = 1349 ms
Depth 10, board 0, cards 1: score = -2, states = 3849688, time = 723 ms
Depth 10, board 1, cards 0: score = 20, states = 4455509, time = 1176 ms
Depth 10, board 1, cards 1: score = 20, states = 11441922, time = 2147 ms
Depth 10, board 2, cards 0: score = 2, states = 7797659, time = 1657 ms
Depth 10, board 2, cards 1: score = -1, states = 14870582, time = 2830 ms

Depth 11, board 0, cards 0: score = 4, states = 39597339, time = 7671 ms
Depth 11, board 0, cards 1: score = 3, states = 22030715, time = 4165 ms
Depth 11, board 1, cards 0: score = 69, states = 25956602, time = 4676 ms
Depth 11, board 1, cards 1: score = 24, states = 38547446, time = 6926 ms
Depth 11, board 2, cards 0: score = 41, states = 25563336, time = 4735 ms
Depth 11, board 2, cards 1: score = 3, states = 70397962, time = 13035 ms

Total states: 291875625
Total time: 71626 ms


Quiescence search:

Depth 1, board 0, cards 0: score = 2, states = 13 + 0, time = 0 ms
Depth 1, board 0, cards 1: score = 2, states = 12 + 0, time = 0 ms
Depth 1, board 1, cards 0: score = 25, states = 19 + 5, time = 0 ms
Depth 1, board 1, cards 1: score = 24, states = 17 + 10, time = 0 ms
Depth 1, board 2, cards 0: score = 2, states = 11 + 1, time = 0 ms
Depth 1, board 2, cards 1: score = 2, states = 17 + 0, time = 0 ms

Depth 2, board 0, cards 0: score = 0, states = 55 + 5, time = 4 ms
Depth 2, board 0, cards 1: score = 0, states = 48 + 0, time = 0 ms
Depth 2, board 1, cards 0: score = 24, states = 69 + 72, time = 0 ms
Depth 2, board 1, cards 1: score = 22, states = 64 + 46, time = 0 ms
Depth 2, board 2, cards 0: score = 0, states = 48 + 31, time = 0 ms
Depth 2, board 2, cards 1: score = 0, states = 66 + 17, time = 0 ms

Depth 3, board 0, cards 0: score = 3, states = 306 + 26, time = 0 ms
Depth 3, board 0, cards 1: score = 2, states = 274 + 11, time = 0 ms
Depth 3, board 1, cards 0: score = 26, states = 478 + 152, time = 0 ms
Depth 3, board 1, cards 1: score = 22, states = 387 + 91, time = 0 ms
Depth 3, board 2, cards 0: score = 2, states = 314 + 140, time = 0 ms
Depth 3, board 2, cards 1: score = 2, states = 376 + 56, time = 0 ms

Depth 4, board 0, cards 0: score = 1, states = 1160 + 1036, time = 0 ms
Depth 4, board 0, cards 1: score = 0, states = 786 + 81, time = 0 ms
Depth 4, board 1, cards 0: score = 25, states = 2106 + 2905, time = 4 ms
Depth 4, board 1, cards 1: score = 22, states = 2198 + 2472, time = 4 ms
Depth 4, board 2, cards 0: score = 1, states = 985 + 1078, time = 4 ms
Depth 4, board 2, cards 1: score = 0, states = 1254 + 1227, time = 0 ms

Depth 5, board 0, cards 0: score = 3, states = 5846 + 2998, time = 4 ms
Depth 5, board 0, cards 1: score = 2, states = 3906 + 670, time = 0 ms
Depth 5, board 1, cards 0: score = 25, states = 6774 + 3606, time = 4 ms
Depth 5, board 1, cards 1: score = 22, states = 8823 + 4574, time = 4 ms
Depth 5, board 2, cards 0: score = 1, states = 3490 + 3366, time = 4 ms
Depth 5, board 2, cards 1: score = 1, states = 7963 + 5926, time = 8 ms

Depth 6, board 0, cards 0: score = 1, states = 29650 + 36920, time = 52 ms
Depth 6, board 0, cards 1: score = 0, states = 12051 + 3651, time = 8 ms
Depth 6, board 1, cards 0: score = 24, states = 36979 + 62283, time = 44 ms
Depth 6, board 1, cards 1: score = 21, states = 40813 + 64904, time = 44 ms
Depth 6, board 2, cards 0: score = 21, states = 45424 + 104829, time = 56 ms
Depth 6, board 2, cards 1: score = 1, states = 43245 + 101060, time = 52 ms

Depth 7, board 0, cards 0: score = 1, states = 120827 + 89929, time = 92 ms
Depth 7, board 0, cards 1: score = 0, states = 59393 + 25779, time = 28 ms
Depth 7, board 1, cards 0: score = 24, states = 109645 + 74521, time = 52 ms
Depth 7, board 1, cards 1: score = 22, states = 207234 + 126561, time = 136 ms
Depth 7, board 2, cards 0: score = 20, states = 80646 + 127591, time = 72 ms
Depth 7, board 2, cards 1: score = 1, states = 119728 + 231461, time = 128 ms

Depth 8, board 0, cards 0: score = 1, states = 350277 + 587443, time = 380 ms
Depth 8, board 0, cards 1: score = -1, states = 187687 + 110991, time = 104 ms
Depth 8, board 1, cards 0: score = 24, states = 650155 + 1296313, time = 600 ms
Depth 8, board 1, cards 1: score = 22, states = 527859 + 694627, time = 412 ms
Depth 8, board 2, cards 0: score = 21, states = 211591 + 424042, time = 216 ms
Depth 8, board 2, cards 1: score = 0, states = 768759 + 2356784, time = 1001 ms

Depth 9, board 0, cards 0: score = 0, states = 1942028 + 2057221, time = 1480 ms
Depth 9, board 0, cards 1: score = -1, states = 1051051 + 613288, time = 592 ms
Depth 9, board 1, cards 0: score = 24, states = 1453996 + 1497213, time = 852 ms
Depth 9, board 1, cards 1: score = 23, states = 2036839 + 1207112, time = 912 ms
Depth 9, board 2, cards 0: score = 20, states = 615188 + 644975, time = 380 ms
Depth 9, board 2, cards 1: score = 1, states = 4999349 + 10825627, time = 5244 ms

Depth 10, board 0, cards 0: score = 1, states = 5920673 + 10879758, time = 5868 ms
Depth 10, board 0, cards 1: score = -1, states = 2612148 + 2160441, time = 1708 ms
Depth 10, board 1, cards 0: score = 67, states = 6086032 + 8679490, time = 4140 ms
Depth 10, board 1, cards 1: score = 23, states = 7975548 + 10353192, time = 5364 ms
Depth 10, board 2, cards 0: score = 25, states = 3903139 + 7771174, time = 3564 ms
Depth 10, board 2, cards 1: score = 0, states = 12195065 + 41834443, time = 16525 ms

Depth 11, board 0, cards 0: score = 1, states = 34979971 + 40220304, time = 24650 ms
Depth 11, board 0, cards 1: score = -1, states = 12658183 + 9463111, time = 7801 ms
Depth 11, board 1, cards 0: score = 68, states = 23320947 + 15725288, time = 10377 ms
Depth 11, board 1, cards 1: score = 23, states = 20126005 + 15102459, time = 9881 ms
Depth 11, board 2, cards 0: score = 24, states = 7547602 + 8363577, time = 4380 ms
Depth 11, board 2, cards 1: score = 1, states = 59869117 + 167571555, time = 68489 ms

Total states: 212942709 + 361520519 = 574463228
Total time: 191882 ms


Move reordering:

Depth 1, board 0, cards 0: score = 2, states = 13 + 0, time = 0 ms
Depth 1, board 0, cards 1: score = 2, states = 12 + 0, time = 0 ms
Depth 1, board 1, cards 0: score = 25, states = 19 + 5, time = 0 ms
Depth 1, board 1, cards 1: score = 24, states = 17 + 10, time = 0 ms
Depth 1, board 2, cards 0: score = 2, states = 11 + 1, time = 4 ms
Depth 1, board 2, cards 1: score = 2, states = 17 + 0, time = 0 ms

Depth 2, board 0, cards 0: score = 0, states = 55 + 5, time = 0 ms
Depth 2, board 0, cards 1: score = 0, states = 48 + 0, time = 0 ms
Depth 2, board 1, cards 0: score = 24, states = 69 + 72, time = 0 ms
Depth 2, board 1, cards 1: score = 22, states = 77 + 62, time = 0 ms
Depth 2, board 2, cards 0: score = 0, states = 48 + 31, time = 0 ms
Depth 2, board 2, cards 1: score = 0, states = 67 + 17, time = 0 ms

Depth 3, board 0, cards 0: score = 3, states = 306 + 26, time = 0 ms
Depth 3, board 0, cards 1: score = 2, states = 263 + 13, time = 0 ms
Depth 3, board 1, cards 0: score = 26, states = 476 + 153, time = 4 ms
Depth 3, board 1, cards 1: score = 22, states = 412 + 137, time = 4 ms
Depth 3, board 2, cards 0: score = 2, states = 315 + 139, time = 0 ms
Depth 3, board 2, cards 1: score = 2, states = 389 + 62, time = 0 ms

Depth 4, board 0, cards 0: score = 1, states = 1153 + 1014, time = 0 ms
Depth 4, board 0, cards 1: score = 0, states = 785 + 77, time = 0 ms
Depth 4, board 1, cards 0: score = 25, states = 2055 + 2870, time = 4 ms
Depth 4, board 1, cards 1: score = 22, states = 2219 + 2566, time = 0 ms
Depth 4, board 2, cards 0: score = 1, states = 986 + 1038, time = 0 ms
Depth 4, board 2, cards 1: score = 0, states = 1347 + 1409, time = 0 ms

Depth 5, board 0, cards 0: score = 3, states = 6050 + 3009, time = 4 ms
Depth 5, board 0, cards 1: score = 2, states = 3904 + 691, time = 4 ms
Depth 5, board 1, cards 0: score = 25, states = 6764 + 3603, time = 4 ms
Depth 5, board 1, cards 1: score = 22, states = 9417 + 4736, time = 4 ms
Depth 5, board 2, cards 0: score = 1, states = 3481 + 3386, time = 4 ms
Depth 5, board 2, cards 1: score = 1, states = 8271 + 6240, time = 8 ms

Depth 6, board 0, cards 0: score = 1, states = 29825 + 35297, time = 36 ms
Depth 6, board 0, cards 1: score = 0, states = 12945 + 4297, time = 8 ms
Depth 6, board 1, cards 0: score = 24, states = 36312 + 62188, time = 40 ms
Depth 6, board 1, cards 1: score = 21, states = 40757 + 62553, time = 40 ms
Depth 6, board 2, cards 0: score = 21, states = 44929 + 100537, time = 60 ms
Depth 6, board 2, cards 1: score = 1, states = 44100 + 98930, time = 56 ms

Depth 7, board 0, cards 0: score = 1, states = 123957 + 89182, time = 88 ms
Depth 7, board 0, cards 1: score = 0, states = 79008 + 33013, time = 36 ms
Depth 7, board 1, cards 0: score = 24, states = 111434 + 75665, time = 56 ms
Depth 7, board 1, cards 1: score = 22, states = 212295 + 126035, time = 116 ms
Depth 7, board 2, cards 0: score = 20, states = 80100 + 122326, time = 76 ms
Depth 7, board 2, cards 1: score = 1, states = 124161 + 231732, time = 128 ms

Depth 8, board 0, cards 0: score = 1, states = 349229 + 568467, time = 392 ms
Depth 8, board 0, cards 1: score = -1, states = 223389 + 135410, time = 128 ms
Depth 8, board 1, cards 0: score = 24, states = 649755 + 1282719, time = 660 ms
Depth 8, board 1, cards 1: score = 22, states = 529962 + 709275, time = 432 ms
Depth 8, board 2, cards 0: score = 21, states = 207345 + 406451, time = 240 ms
Depth 8, board 2, cards 1: score = 0, states = 849136 + 2389466, time = 1136 ms

Depth 9, board 0, cards 0: score = 0, states = 1975483 + 2096877, time = 1549 ms
Depth 9, board 0, cards 1: score = -1, states = 1251455 + 746239, time = 700 ms
Depth 9, board 1, cards 0: score = 24, states = 1530890 + 1510452, time = 980 ms
Depth 9, board 1, cards 1: score = 23, states = 2304421 + 1348258, time = 1048 ms
Depth 9, board 2, cards 0: score = 20, states = 620408 + 619738, time = 412 ms
Depth 9, board 2, cards 1: score = 1, states = 5355131 + 11566760, time = 5905 ms

Depth 10, board 0, cards 0: score = 1, states = 5999669 + 10734354, time = 6369 ms
Depth 10, board 0, cards 1: score = -1, states = 2922935 + 2549008, time = 1960 ms
Depth 10, board 1, cards 0: score = 67, states = 6248805 + 8445889, time = 4765 ms
Depth 10, board 1, cards 1: score = 23, states = 8071287 + 10317736, time = 6133 ms
Depth 10, board 2, cards 0: score = 25, states = 4023136 + 7951287, time = 4692 ms
Depth 10, board 2, cards 1: score = 0, states = 13041210 + 43309226, time = 19065 ms

Depth 11, board 0, cards 0: score = 1, states = 34441315 + 39368314, time = 25958 ms
Depth 11, board 0, cards 1: score = -1, states = 12685382 + 9990015, time = 7997 ms
Depth 11, board 1, cards 0: score = 68, states = 13570816 + 8486694, time = 5952 ms
Depth 11, board 1, cards 1: score = 23, states = 21235534 + 14515291, time = 10372 ms
Depth 11, board 2, cards 0: score = 24, states = 7974595 + 8688042, time = 5424 ms
Depth 11, board 2, cards 1: score = 1, states = 69900087 + 200903521, time = 83614 ms

Total states: 216950244 + 389712616 = 606662860
Total time: 211525 ms
Total time: 125053 ms (bitboard optimizations)
Total time: 91660 ms (more efficient scoring)
Total time: 86833 ms (simplified bitboards)

 */
public class TestSearchDepthPerformance {

    static String EMPTY_BOARD =
            "bbBbb" +
            "....." +
            "....." +
            "....." +
            "wwWww";

    static String BOARD_WIN_AT_13 =
            "b.Bbb" +
            "....." +
            ".b..." +
            ".wwW." +
            "w...w";

    static String BOARD_CORNERS =
            "bb..." +
            "B...w" +
            "b...w" +
            "b...W" +
            "...ww";

    static String[] BOARDS = {EMPTY_BOARD, BOARD_WIN_AT_13, BOARD_CORNERS};

    public static void main(String ... args) throws Exception {
        Searcher.LOGGING = false;

        new TestSearchDepthPerformance().testDepth(10);
    }

    private void testDepth(int maxDepth) {
        long totalTime = System.currentTimeMillis();
        long totalStates = 0, totalQStates = 0;

        for (int depth = 1; depth <= 11; ++depth) {
            for (int board = 0; board < 3; ++board) {
                for (int cards = 0; cards < 2; ++cards) {
                    Searcher searcher = new Searcher(depth, 26);

                    searcher.setState(0, BOARDS[board], cards == 0 ?
                            new CardState(new Card[][] {{Card.Monkey, Card.Crane}, {Card.Tiger, Card.Crab}}, Card.Dragon) :
                            new CardState(new Card[][] {{Card.Monkey, Card.Frog}, {Card.Elephant, Card.Boar}}, Card.Cobra));

                    long time = System.currentTimeMillis();
                    int score = searcher.start(Integer.MAX_VALUE);
                    time = System.currentTimeMillis() - time;

                    long states = searcher.stats.getStatesEvaluated();
                    long qStates = searcher.stats.getQuiescenceStatesEvaluated();
                    totalStates += states;
                    totalQStates += qStates;

                    System.out.printf("Depth %d, board %d, cards %d: score = %d, states = %d + %d, time = %d ms%n", depth, board, cards, score, states, qStates, time);
                }
            }
            System.out.println();
        }

        System.out.printf("Total states: %d + %d = %d%n", totalStates, totalQStates, totalStates+totalQStates);
        System.out.printf("Total time: %d ms%n", System.currentTimeMillis() - totalTime);
    }
}
