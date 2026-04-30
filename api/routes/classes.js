/**
 * @fileoverview Routes pour avoir les infos sur les classes
 * @module routes/classes
 */

const express = require('express');
const router = express.Router();
const classeController = require('../controllers/classeController');
const verifyToken = require('../middlewares/verifyToken');

router.get('/', verifyToken, classeController.getAll);

module.exports = router;