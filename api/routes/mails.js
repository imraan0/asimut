/**
 * @fileoverview Routes du publipostage
 * @module routes/mails
 */

const express = require('express');
const router = express.Router();
const mailController = require('../controllers/mailController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.post('/all',      verifyToken, checkRole('secretariat', 'professeur', 'proviseur'), mailController.mailAll);
router.post('/:id/mail', verifyToken, checkRole('secretariat', 'professeur', 'proviseur'), mailController.mailOne);

module.exports = router;