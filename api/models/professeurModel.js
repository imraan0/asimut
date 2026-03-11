const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Professeur = sequelize.define('professeur', {
    id: {
        type:          DataTypes.INTEGER,
        primaryKey:    true,
        autoIncrement: true
    },
    nom: {
        type:      DataTypes.STRING(100),
        allowNull: false
    },
    prenom: {
        type:      DataTypes.STRING(100),
        allowNull: false
    }
}, {
    timestamps:  false,  // pas de created_at sur cette table
    underscored: true
})

module.exports = Professeur