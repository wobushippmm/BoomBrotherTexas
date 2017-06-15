var PickResult = {};

PickResult.FlowerCount      = 4;
PickResult.Flower_heart     = 0; // 红心
PickResult.Flower_spade     = 1; // 黑桃
PickResult.Flower_club      = 2; // 梅花
PickResult.Flower_diamond   = 3; // 方块

PickResult.PointCount       = 14;
PickResult.Point_1          = 0;
PickResult.Point_2          = 1;
PickResult.Point_3          = 2;
PickResult.Point_4          = 3;
PickResult.Point_5          = 4;
PickResult.Point_6          = 5;
PickResult.Point_7          = 6;
PickResult.Point_8          = 7;
PickResult.Point_9          = 8;
PickResult.Point_10         = 9;
PickResult.Point_J          = 10;
PickResult.Point_Q          = 11;
PickResult.Point_K          = 12;
PickResult.Point_A          = 13;

PickResult.Pattern_high_card        = 0; // 高牌
PickResult.Pattern_one_pair         = 1; // 一对
PickResult.Pattern_two_pair         = 2; // 两对
PickResult.Pattern_three_a_kind     = 3; // 三条
PickResult.Pattern_straight         = 4; // 顺子
PickResult.Pattern_flush            = 5; // 同花
PickResult.Pattern_full_house       = 6; // 葫芦
PickResult.Pattern_four_a_kind      = 7; // 四条
PickResult.Pattern_straight_flush   = 8; // 同花顺
PickResult.Pattern_royal_flush      = 9; // 皇家同花顺

PickResult.Shift    = 100; // 移位

PickResult.type2Name    = ["高牌", "一对", "两对", "三条", "顺子", "同花", "葫芦", "四条", "同花顺", "皇家同花顺"];

// 从数字得到牌
PickResult.getSuitAndPoint = function(num){
    var suit = parseInt(num/this.Shift);
    var point = num%this.Shift;
    if(suit < 0 || point < 0){
        cc.error('pick result', suit, point, num);
    }
    return {'suit':suit, 'point':point};
};

// 从牌得到数字
PickResult.getNumber = function(card){
    var num = card.suit*this.Shift+card.point;
    return num;
};

// 生成一个新牌组
PickResult.NewCardSet = function(){
    var cards = [];
    for(var i = 0; i < this.FlowerCount; i++){
        cards[i] = [];
        for(var j = 0; j < this.PointCount; j++){
            cards[i][j] = 0;
        }
    }
    return cards;
};

// 复制牌组
PickResult.copy = function(src){
    var des = this.NewCardSet();
    for(var i = 0; i < this.FlowerCount; i++){
        for(var j = 0; j < this.PointCount; j++){
            des[i][j] = src[i][j];
        }
    }
    return des;
};

// 合并牌组
PickResult.combo = function(cards1, cards2){
    var cards = this.NewCardSet();
    for(var i = 0; i < this.FlowerCount; i++){
        for(var j = 0; j < this.PointCount; j++){
            cards[i][j] = cards1[i][j] + cards2[i][j];
        }
    }
    for(var i = 0; i < this.FlowerCount; i++){
        if(cards[i][this.Point_A] != cards[i][this.Point_1]){
            cards[i][this.Point_A] = cards[i][this.Point_1] = 1;
        }
    }
    return cards;
};

// 计算结果
PickResult.pick = function(srcCards){
    var cards = this.NewCardSet();
    // 将牌转成牌组
    for(var i = 0; i < srcCards.length; i++){
        cards[parseInt(srcCards[i]/this.Shift)][srcCards[i]%this.Shift] = 1;
    }
    for(var i = 0; i < this.FlowerCount; i++){
        if(cards[i][this.Point_A] != cards[i][this.Point_1]){
            cards[i][this.Point_A] = cards[i][this.Point_1] = 1;
        }
    }
    var res = this.pickRoyalFlush(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickStraightFlush(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickFourAKind(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickFullHouse(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickFlush(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickStraight(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickThreeAKind(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickTwoPair(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickOnePair(cards);
    if(res[0] > 0){
        return res;
    }
    res = this.pickHighCard(cards);
    return res;
};

// 皇家同花顺
PickResult.pickRoyalFlush = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = 0; i < this.FlowerCount; i++){
        if(cards[i][this.Point_A] + cards[i][this.Point_K] + cards[i][this.Point_Q] + cards[i][this.Point_J] + cards[i][this.Point_10] == 5){
            res[0] = this.Pattern_royal_flush;
            res[1] = i * this.Shift + this.Point_A;
            res[2] = i * this.Shift + this.Point_K;
            res[3] = i * this.Shift + this.Point_Q;
            res[4] = i * this.Shift + this.Point_J;
            res[5] = i * this.Shift + this.Point_10;
            return res;
        }
    }
    return res;
};

// 同花顺
PickResult.pickStraightFlush = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 3; i--){
        for(var j = 0; j < this.FlowerCount; j++){
            if(cards[j][i] + cards[j][i-1] + cards[j][i-2] + cards[j][i-3] + cards[j][i-4] == 5){
                res[0] = this.Pattern_straight_flush;
                res[1] = j * this.Shift + i;
                res[2] = j * this.Shift + i - 1;
                res[3] = j * this.Shift + i - 2;
                res[4] = j * this.Shift + i - 3;
                res[5] = j * this.Shift + i - 4;
                return res;
            }
        }
    }
    return res;
};

// 四条
PickResult.pickFourAKind = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 0; i--){
        var num = 0;
        for(var j = 0; j < this.FlowerCount; j++){
            num += cards[j][i];
        }
        if(num == 4){
            res[0] = this.Pattern_four_a_kind;
            res[1] = this.Flower_heart * this.Shift + i;
            res[2] = this.Flower_spade * this.Shift + i;
            res[3] = this.Flower_club * this.Shift + i;
            res[4] = this.Flower_diamond * this.Shift + i;
            for(var j = this.PointCount-1; j > 0; j--){
                if(j != i){
                    for(var k = 0; k < this.FlowerCount; k++){
                        if(cards[k][j] == 1){
                            res[5] = k * this.Shift + j;
                            return res;
                        }
                    }
                }
            }
        }
    }
    return res;
};

// 葫芦
PickResult.pickFullHouse = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 0; i--){
        var num = 0;
        for(var j = 0; j < this.FlowerCount; j++){
            num += cards[j][i];
        }
        if(num == 3){
            for(var k = this.PointCount-1; k > 0; k--){
                if(k == i){
                    continue;
                }
                num = 0;
                for(var j = 0; j < this.FlowerCount; j++){
                    num += cards[j][k];
                }
                if(num >= 2){
                    res[0] = this.Pattern_full_house;
                    var n = 1;
                    var l = 4;
                    for(var m = 0; m < this.FlowerCount; m++){
                        if(cards[m][i] == 1){
                            res[n++] = m * this.Shift + i;
                        }
                        if(l <= 5 && cards[m][k] == 1){
                            res[l++] = m * this.Shift + k;
                        }
                    }
                    return res;
                }
            }
        }
    }
    return res;
};

// 同花
PickResult.pickFlush = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    var a = [0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 0; i--){
        a[this.Flower_heart] += cards[this.Flower_heart][i];
        a[this.Flower_spade] += cards[this.Flower_spade][i];
        a[this.Flower_club] += cards[this.Flower_club][i];
        a[this.Flower_diamond] += cards[this.Flower_diamond][i];
        var k = -1;
        if(a[this.Flower_heart] == 5){
            k = 0;
        }
        if(a[this.Flower_spade] == 5){
            k = 1;
        }
        if(a[this.Flower_club] == 5){
            k = 2;
        }
        if(a[this.Flower_diamond] == 5){
            k = 3;
        }
        if(k > -1){
            var n = 1;
            res[0] = this.Pattern_flush;
            for(var j = this.PointCount-1; j > 0; j--){
                if(cards[k][j] == 1 && n < 6){
                    res[n++] = k * this.Shift + j;
                }
            }
            return res;
        }
    }
    return res;
};

// 顺子
PickResult.pickStraight = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    var a = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    for(var i = 0; i < this.PointCount; i++){
        a[i] = cards[this.Flower_heart][i] | cards[this.Flower_spade][i] | cards[this.Flower_club][i] | cards[this.Flower_diamond][i];
    }
    for(var i = this.PointCount-1; i > 3; i--){
        if(a[i] + a[i-1] + a[i-2] + a[i-3] + a[i-4] == 5){
            res[0] = this.Pattern_straight;
            for(var j = 0; j < this.FlowerCount; j++){
                if(cards[j][i] == 1){
                    res[1] = j * this.Shift + i;
                }
                if(cards[j][i-1] == 1){
                    res[2] = j * this.Shift + i - 1;
                }
                if(cards[j][i-2] == 1){
                    res[3] = j * this.Shift + i - 2;
                }
                if(cards[j][i-3] == 1){
                    res[4] = j * this.Shift + i - 3;
                }
                if(cards[j][i-4] == 1){
                    res[5] = j * this.Shift + i - 4;
                }
            }
            return res;
        }
    }
    return res;
};

// 三条
PickResult.pickThreeAKind = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 0; i--){
        var num = 0;
        for(var j = 0; j < this.FlowerCount; j++){
            num += cards[j][i];
        }
        if(num == 3){
            res[0] = this.Pattern_three_a_kind;
            var l = 1;
            for(var j = 0; j < this.FlowerCount; j++){
                if(cards[j][i] == 1){
                    res[l++] = j * this.Shift + i;
                }
            }
            for(var m = this.PointCount-1; m > 0; m--){
                for(var n = 0; n < this.FlowerCount; n++){
                    if(cards[n][m] == 1 && m != i && l < 6){
                        res[l++] = n * this.Shift + m;
                    }
                }
            }
            return res;
        }
    }
    return res;
};

// 两对
PickResult.pickTwoPair = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    var k = 0;
    for(var i = this.PointCount-1; i > 0; i--){
        if(cards[this.Flower_heart][i] + cards[this.Flower_spade][i] + cards[this.Flower_club][i] + cards[this.Flower_diamond][i] == 2){
            if(k == 0){
                k = i;
            }
            else{
                var n = 1;
                var m = 3;
                res[0] = this.Pattern_two_pair;
                for(var j = 0; j < this.FlowerCount; j++){
                    if(cards[j][k] == 1){
                        res[n++] = j * this.Shift + k;
                    }
                    if(cards[j][i] == 1){
                        res[m++] = j * this.Shift + i;
                    }
                }
                for(var j = this.PointCount-1; j > 0; j--){
                    for(var l = 0; l < this.FlowerCount; l++){
                        if(cards[l][j] == 1 && j != k && j != i){
                            res[5] = l * this.Shift + j;
                            return res;
                        }
                    }
                }
            }
        }
    }
    return res;
};

// 一对
PickResult.pickOnePair = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    for(var i = this.PointCount-1; i > 0; i--){
        if(cards[this.Flower_heart][i] + cards[this.Flower_spade][i] + cards[this.Flower_club][i] + cards[this.Flower_diamond][i] == 2){
            res[0] = this.Pattern_one_pair;
            var n = 1;
            for(var j = 0; j < this.FlowerCount; j++){
                if(cards[j][i] == 1){
                    res[n++] = j * this.Shift + i;
                }
            }
            for(var j = this.PointCount-1; j > 0; j--){
                for(var k = 0; k < this.FlowerCount; k++){
                    if(cards[k][j] == 1 && j != i && n < 6){
                        res[n++] = k * this.Shift + j;
                    }
                }
            }
            return res;
        }
    }
    return res;
};

// 高牌
PickResult.pickHighCard = function(cards){
    var res = [0, 0, 0, 0, 0, 0];
    var n = 1;
    res[0] = this.Pattern_high_card;
    for(var i = this.PointCount-1; i > 0; i--){
        for(var j = 0; j < this.FlowerCount; j++){
            if(cards[j][i] == 1 && n < 6){
                res[n++] = j * this.Shift + i;
            }
        }
    }
    return res;
};

module.exports = PickResult;