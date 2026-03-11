const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Option = sequelize.define('option', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nom: {
        type: DataTypes.STRING(100),
        allowNull: false
    },
    type: {
        type: DataTypes.ENUM('langue', 'technique'),
        allowNull: false
    }
}, {
    timestamps: false,
    underscored: true,
})

module.exports = Option