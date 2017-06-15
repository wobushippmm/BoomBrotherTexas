package logic.battle.texasHoldem;

import java.util.ArrayList;

/**
 * 皇家同花顺>同花顺>四条>葫芦>同花>顺子>三条>两对>一对>高牌
 * A>K>Q>J>10>9>8>7>6>5>4>3>2>1
 * 13>12>11>10>9>8>7>6>5>4>3>2>1>0
 * 
 * @author lihebin
 * 牌的id Flower * 100 + Point
 */
public class PickResult {
	public static final int FlowerCount = 4;
	public static final int Flower_heart 	= 0; // 红心
	public static final int Flower_spade 	= 1; // 黑桃
	public static final int Flower_club 	= 2; // 梅花
	public static final int Flower_diamond 	= 3; // 方块
	
	public static final int PointCount = 14;
	public static final int Point_1	= 0;
	public static final int Point_2	= 1;
	public static final int Point_3	= 2;
	public static final int Point_4	= 3;
	public static final int Point_5	= 4;
	public static final int Point_6	= 5;
	public static final int Point_7	= 6;
	public static final int Point_8	= 7;
	public static final int Point_9	= 8;
	public static final int Point_10= 9;
	public static final int Point_J	= 10;
	public static final int Point_Q	= 11;
	public static final int Point_K	= 12;
	public static final int Point_A	= 13;
	
	public static final int Pattern_high_card 	= 0; // 高牌
	public static final int Pattern_one_pair 	= 1; // 一对
	public static final int Pattern_two_pair	= 2; // 两对
	public static final int Pattern_three_a_kind = 3; // 三条
	public static final int Pattern_straight	= 4; // 顺子
	public static final int Pattern_flush		= 5; // 同花
	public static final int Pattern_full_house	= 6; // 葫芦
	public static final int Pattern_four_a_kind = 7; // 四条
	public static final int Pattern_straight_flush = 8; // 同花顺
	public static final int Pattern_royal_flush = 9; // 皇家同花顺
	
	public static final int Shift = 100; // 移位
	
	// 生成一个新牌组
	public static int[][] NewCardSet(){
		int[][] cards = new int[FlowerCount][PointCount];
		for(int i=0; i<FlowerCount; i++){
			for(int j=0; j<PointCount; j++){
				cards[i][j] = 0;
			}
		}
		return cards;
	}
	
	// 复制牌组
	public static int[][] copy(int[][] src){
		int[][] des = NewCardSet();
		for(int i=0; i<FlowerCount; i++){
			for(int j=0; j<PointCount; j++){
				des[i][j] = src[i][j];
			}
		}
		return des;
	}
	
	// 合并牌组
	public static int[][] combo(int[][]cards1, int[][]cards2){
		int[][] cards = NewCardSet();
		for(int i=0; i<FlowerCount; i++){
			for(int j=0; j<PointCount; j++){
				cards[i][j] = cards1[i][j] + cards2[i][j];
			}
		}
		for(int i=0; i<FlowerCount; i++){
			if(cards[i][Point_A] != cards[i][Point_1])cards[i][Point_A] = cards[i][Point_1] = 1;
		}
		return cards;
	}
	// 计算牌型分数
	public static long score(int[] srcCards){
		long v = 0;
		long d = 10000000000l;
		for(int i=0; i<srcCards.length; i++){
			v += (srcCards[i] % 100) * d;
			d /= 100;
		}
		return v;
	}
	public static int[] pick(ArrayList<Integer> pubCards, ArrayList<Integer> plaCards){
		int[][] cards = NewCardSet();
		// 将牌转成牌组
		for(int i=0; i<pubCards.size(); i++){
			cards[(int)(pubCards.get(i) / Shift)][pubCards.get(i) % Shift] = 1;
		}
		for(int i=0; i<plaCards.size(); i++){
			cards[(int)(plaCards.get(i) / Shift)][plaCards.get(i) % Shift] = 1;
		}
		return pick(cards);
	}
	// 计算结果
	public static int[] pick(int[] srcCards){
		int[][] cards = NewCardSet();
		// 将牌转成牌组
		for(int i=0; i<srcCards.length; i++){
			cards[(int)(srcCards[i] / Shift)][srcCards[i] % Shift] = 1;
		}
		return pick(cards);
	}
	public static int[] pick(int[][] cards){
		for(int i=0; i<FlowerCount; i++){
			if(cards[i][Point_A] != cards[i][Point_1])cards[i][Point_A] = cards[i][Point_1] = 1;
		}
		
		int[] res = pickRoyalFlush(cards);
		if(res[0] > 0)return res;
		
		res = pickStraightFlush(cards);
		if(res[0] > 0)return res;
		
		res = pickFourAKind(cards);
		if(res[0] > 0)return res;
		
		res = pickFullHouse(cards);
		if(res[0] > 0)return res;
		
		res = pickFlush(cards);
		if(res[0] > 0)return res;
		
		res = pickStraight(cards);
		if(res[0] > 0)return res;
		
		res = pickThreeAKind(cards);
		if(res[0] > 0)return res;
		
		res = pickTwoPair(cards);
		if(res[0] > 0)return res;
		
		res = pickOnePair(cards);
		if(res[0] > 0)return res;
		
		res = pickHighCard(cards);
		return res;
	}
	
	// 皇家同花顺
	public static int[] pickRoyalFlush(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i=0; i<FlowerCount; i++){
			if(cards[i][Point_A] + cards[i][Point_K] + cards[i][Point_Q] + cards[i][Point_J] + cards[i][Point_10] == 5){
				res[0] = Pattern_royal_flush;
				res[1] = i * Shift + Point_A;
				res[2] = i * Shift + Point_K;
				res[3] = i * Shift + Point_Q;
				res[4] = i * Shift + Point_J;
				res[5] = i * Shift + Point_10;
				return res;
			}
		}
		return res;
	}
	// 同花顺
	public static int[] pickStraightFlush(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i=PointCount-1; i>3; i--){
			for(int j=0; j<FlowerCount; j++){
				if(cards[j][i] + cards[j][i-1] + cards[j][i-2] + cards[j][i-3] + cards[j][i-4] == 5){
					res[0] = Pattern_straight_flush;
					res[1] = j * Shift + i;
					res[2] = j * Shift + i-1;
					res[3] = j * Shift + i-2;
					res[4] = j * Shift + i-3;
					res[5] = j * Shift + i-4;
					return res;
				}
			}
		}
		return res;
	}
	// 四条
	public static int[] pickFourAKind(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i=PointCount-1; i>0; i--){
			int num = 0;
			for(int j=0; j<FlowerCount; j++){
				num += cards[j][i];
			}
			if(num == 4){
				res[0] = Pattern_four_a_kind;
				res[1] = Flower_heart * Shift + i;
				res[2] = Flower_spade * Shift + i;
				res[3] = Flower_club * Shift + i;
				res[4] = Flower_diamond * Shift + i;
				for(int j=PointCount-1; j>0; j--){ // 选单张
					if(j != i){
						for(int k=0; k<FlowerCount; k++){
							if(cards[k][j] == 1){
								res[5] = k * Shift + j;
								return res;
							}
						}
					}
				}
			}
		}
		return res;
	}
	// 葫芦 
	public static int[] pickFullHouse(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i = PointCount-1; i>0; i--){
			int num = 0;
			for(int j=0; j<FlowerCount; j++){
				num += cards[j][i];
			}
			if(num == 3){ // 找到三张牌
				for(int k=PointCount-1; k>0; k--){
					if(k == i)continue;
					num = 0;
					for(int j=0; j<FlowerCount; j++){
						num += cards[j][k];
					}
					if(num >= 2){ // 找到二张牌
						res[0] = Pattern_full_house;
						int n = 1;
						int l = 4;
						for(int m=0; m<FlowerCount; m++){
							if(cards[m][i] == 1){
								res[n++] = m * Shift + i;
							}
							if(l <= 5 && cards[m][k] == 1){
								res[l++] = m * Shift + k;
							}
						}
						return res;
					}
				}
			}
		}
		return res;
	}
	// 同花
	public static int[] pickFlush(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		int[] a = {0, 0, 0, 0};
		for(int i=PointCount-1; i>0; i--){
			a[Flower_heart] += cards[Flower_heart][i];
			a[Flower_spade] += cards[Flower_spade][i];
			a[Flower_club] += cards[Flower_club][i];
			a[Flower_diamond] += cards[Flower_diamond][i];
			int k = -1;
			if(a[Flower_heart] == 5)k = 0;
			if(a[Flower_spade] == 5)k = 1;
			if(a[Flower_club] == 5)k = 2;
			if(a[Flower_diamond] == 5)k = 3;
			if(k > -1){ // 找到同花
				int n = 1;
				res[0] = Pattern_flush;
				for(int j=PointCount-1; j>0; j--){
					if(cards[k][j] == 1 && n<6){
						res[n++] = k * Shift + j;
					}
				}
				return res;
			}
		}
		return res;
	}
	// 顺子
	public static int[] pickStraight(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		int[] a = new int[PointCount];
		for(int i=0; i<PointCount; i++){
			a[i] = cards[Flower_heart][i] | cards[Flower_spade][i] | cards[Flower_club][i] | cards[Flower_diamond][i];
		}
		for(int i=PointCount-1; i>3; i--){
			if(a[i] + a[i-1] + a[i-2] + a[i-3] + a[i-4] == 5){
				res[0] = Pattern_straight;
				for(int j=0; j<FlowerCount; j++){
					if(cards[j][i] == 1)res[1] = j * Shift + i;
					if(cards[j][i-1] == 1)res[2] = j * Shift + i-1;
					if(cards[j][i-2] == 1)res[3] = j * Shift + i-2;
					if(cards[j][i-3] == 1)res[4] = j * Shift + i-3;
					if(cards[j][i-4] == 1)res[5] = j * Shift + i-4;
				}
				return res;
			}
		}
		return res;
	}
	// 三条
	public static int[] pickThreeAKind(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i = PointCount-1; i>0; i--){
			int num = 0;
			for(int j=0; j<FlowerCount; j++){
				num += cards[j][i];
			}
			if(num == 3){ 
				res[0] = Pattern_three_a_kind;
				int l=1;
				for(int j=0; j<FlowerCount; j++){
					if(cards[j][i] == 1){
						res[l++] = j * Shift + i;
					}
				}
				for(int m=PointCount-1; m>0; m--){
					for(int n=0; n<FlowerCount; n++){
						if(cards[n][m] == 1 && m != i && l<6){
							res[l++] = n * Shift + m;
						}
					}
				}
				return res;
			}
		}
		return res;		
	}
	// 两对
	public static int[] pickTwoPair(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		int k=0;
		for(int i = PointCount-1; i>0; i--){
			if(cards[Flower_heart][i] + cards[Flower_spade][i] + cards[Flower_club][i] + cards[Flower_diamond][i] == 2){
				if(k == 0){
					k = i;
				}else{
					int n=1;
					int m=3;
					res[0] = Pattern_two_pair;
					for(int j=0; j<FlowerCount; j++){
						if(cards[j][k] == 1){
							res[n++] = j * Shift + k;
						}
						if(cards[j][i] == 1){
							res[m++] = j * Shift + i;
						}
					}
					for(int j=PointCount-1; j>0; j--){
						for(int l=0; l<FlowerCount; l++){
							if(cards[l][j] == 1 && j != k && j!= i){
								res[5] = l * Shift + j;
								return res;
							}
						}
					}
				}
			}
		}
		return res;
	}
	// 一对
	public static int[] pickOnePair(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		for(int i = PointCount-1; i>0; i--){
			if(cards[Flower_heart][i] + cards[Flower_spade][i] + cards[Flower_club][i] + cards[Flower_diamond][i] == 2){
				res[0] = Pattern_one_pair;
				int n = 1;
				for(int j=0; j<FlowerCount; j++){
					if(cards[j][i] == 1){
						res[n++] = j * Shift + i;
					}
				}
				for(int j=PointCount-1; j>0; j--){
					for(int k=0; k<FlowerCount; k++){
						if(cards[k][j] == 1 && j != i && n < 6){
							res[n++] = k * Shift + j;
						}
					}
				}
				return res;
			}
		}
		return res;
	}
	// 高牌
	public static int[] pickHighCard(int[][] cards){
		int[] res = {0, 0, 0, 0, 0, 0};
		int n = 1;
		res[0] = Pattern_high_card;
		for(int i = PointCount-1; i>0; i--){
			for(int j=0; j<FlowerCount; j++){
				if(cards[j][i] == 1 && n < 6){
					res[n++] = j * Shift + i;
				}
			}
		}
		return res;
	}
}  
