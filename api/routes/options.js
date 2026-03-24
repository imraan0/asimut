/**
 * @fileoverview Routes des options
 * @module routes/options
 */

const express = require('express');
const router = express.Router();
const optionController = require('../controllers/optionController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/',           verifyToken, checkRole('secretariat', 'proviseur', 'professeur', 'eleve'), optionController.getAll);
router.post('/',          verifyToken, checkRole('secretariat'),                                     optionController.create);
router.post('/affecter',  verifyToken, checkRole('secretariat'),                                     optionController.affecter);
router.delete('/retirer', verifyToken, checkRole('secretariat'),                                     optionController.retirer);
router.delete('/:id',     verifyToken, checkRole('secretariat'),                                     optionController.remove);

module.exports = router;