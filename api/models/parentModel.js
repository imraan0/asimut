const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Parent = sequelize.define('parent', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nom:{
        type: DataTypes.STRING(100),
        allowNull: false
    },
    prenom:{
        type: DataTypes.STRING(100),
        allowNull: false
    },
    email:{
        type: DataTypes.STRING(255),
        allowNull: false,
        validate: {
            isEmail: true
        }
    }
}, {
    timestamps: false,
    underscored: true
})

module.exports = Parent