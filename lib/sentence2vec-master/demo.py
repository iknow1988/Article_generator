#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Licensed under the GNU LGPL v2.1 - http://www.gnu.org/licenses/lgpl.html


"""

"""

import logging
import sys
import os
from word2vec import Word2Vec, Sent2Vec, LineSentence

logging.basicConfig(format='%(asctime)s : %(threadName)s : %(levelname)s : %(message)s', level=logging.INFO)
logging.info("running %s" % " ".join(sys.argv))

#input_file = 'paragraph.txt'
input_file = str(sys.argv[1]);
model = Word2Vec(LineSentence(input_file), size=50, window=8, sg=0, min_count=4, workers=12)
model.save(input_file + '.model')
#model.save_word2vec_format(input_file + '.vec')

sent_file = input_file
model = Sent2Vec(LineSentence(sent_file), model_file=input_file + '.model', iteration=10, workers=8)
#model = Sent2Vec(LineSentence(sent_file), model_file='/Users/wkleung/Downloads/word2vec/trunk/GoogleNews-vectors-negative300.bin')
model.save_sent2vec_format(sent_file + '.vec')

program = os.path.basename(sys.argv[0])
logging.info("finished running %s" % program)