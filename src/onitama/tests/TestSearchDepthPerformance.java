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
        long totalStates = 0;

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

                    long states = searcher.fullStatesEvaluated;
                    totalStates += states;

                    System.out.printf("Depth %d, board %d, cards %d: score = %d, states = %d, time = %d ms%n", depth, board, cards, score, states, time);
                }
            }
            System.out.println();
        }

        System.out.printf("Total states: %d%n", totalStates);
        System.out.printf("Total time: %d ms%n", System.currentTimeMillis() - totalTime);
    }
}
