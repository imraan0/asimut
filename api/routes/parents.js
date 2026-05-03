/**
 * @fileoverview Routes des parents
 * @module routes/parents
 */

const express = require('express');
const router = express.Router();
const parentController = require('../controllers/parentController');
const mailController = require('../controllers/mailController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/',          verifyToken, checkRole('secretariat', 'professeur', 'proviseur'), parentController.getAll);
router.post('/mail',     verifyToken, checkRole('secretariat', 'professeur', 'proviseur'), mailController.mailAll);
router.post('/:id/mail', verifyToken, checkRole('secretariat', 'professeur', 'proviseur'), mailController.mailOne);

module.exports = router;